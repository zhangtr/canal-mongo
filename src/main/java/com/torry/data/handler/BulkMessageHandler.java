package com.torry.data.handler;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteResult;
import com.mongodb.DBObject;
import com.torry.data.common.EventData;
import com.torry.data.util.DBConvertUtil;
import com.torry.data.util.SpringUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * binlog消息批处理类
 *
 * @author zhangtongrui
 * @date 2017/12/12
 */

public class BulkMessageHandler implements MessageHandler {
    private final static Logger logger = LoggerFactory.getLogger(BulkMessageHandler.class);
    //行数据日志
    private static String row_format = "binlog[{}:{}] , name[{},{}] , eventType : {} , executeTime : {} , delay : {}ms";
    //事务日志
    private static String transaction_format = "binlog[{}:{}] , executeTime : {} , delay : {}ms";
    //批处理执行日志
    private static String execute_format = "bulk command execute over,available:{} insert:{} update:{} remove:{}";
    //数据存储耗时日志
    private static String execute_time_format = "bulk handler executeTime ,overData:{}ms overNaive:{}ms overComplete:{}ms";

    //处理数据集
    private List<Entry> entrys;

    public BulkMessageHandler(List<Entry> entrys) {
        this.entrys = entrys;
    }

    private Map<String, List<EventData>> dataMap = new HashMap<>();
    private List<EventData> dataList = new ArrayList<>();

    public boolean execute() throws Exception {
        MongoTemplate mongoTemplate = SpringUtil.getBean("naiveMongoTemplate", MongoTemplate.class);
        long start = System.currentTimeMillis();
        //遍历数据
        for (Entry entry : entrys) {
            long executeTime = entry.getHeader().getExecuteTime();
            long startTime = System.currentTimeMillis();
            long delayTime = startTime - executeTime;
            //打印事务开始结束信息
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN) {
                    CanalEntry.TransactionBegin begin;
                    try {
                        begin = CanalEntry.TransactionBegin.parseFrom(entry.getStoreValue());
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException("parse event has an error , data:" + entry.toString(), e);
                    }
                    // 打印事务头信息，事务耗时
                    logger.info(transaction_format, entry.getHeader().getLogfileName(), String.valueOf(entry.getHeader().getLogfileOffset()),
                            String.valueOf(entry.getHeader().getExecuteTime()), String.valueOf(delayTime));
                    logger.info(" BEGIN ----> Thread id: {}", begin.getThreadId());
                } else if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                    CanalEntry.TransactionEnd end;
                    try {
                        end = CanalEntry.TransactionEnd.parseFrom(entry.getStoreValue());
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException("parse event has an error , data:" + entry.toString(), e);
                    }
                    // 打印事务提交信息，事务id
                    logger.info(" END ----> transaction id: {}", end.getTransactionId());
                    logger.info(transaction_format, entry.getHeader().getLogfileName(), String.valueOf(entry.getHeader().getLogfileOffset()),
                            String.valueOf(entry.getHeader().getExecuteTime()), String.valueOf(delayTime));
                }
                continue;
            }
            //保存事务内变动数据
            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                CanalEntry.RowChange rowChage;
                try {
                    rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                } catch (Exception e) {
                    throw new RuntimeException("parse event has an error , data:" + entry.toString(), e);
                }
                CanalEntry.EventType eventType = rowChage.getEventType();
                logger.info(row_format, entry.getHeader().getLogfileName(), String.valueOf(entry.getHeader().getLogfileOffset()),
                        entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                        eventType, String.valueOf(entry.getHeader().getExecuteTime()), String.valueOf(delayTime));
                if (eventType == CanalEntry.EventType.ERASE || eventType == CanalEntry.EventType.TRUNCATE) {
                    logger.info(" sql ----> " + rowChage.getSql());
                    //包含清表操作，不做处理返回false
                    return false;
                } else if (eventType == CanalEntry.EventType.QUERY || rowChage.getIsDdl()) {
                    logger.info(" sql ----> " + rowChage.getSql());
                    continue;
                }
                for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                    String schemaName = entry.getHeader().getSchemaName();
                    String tableName = entry.getHeader().getTableName();
                    //可添加处理分表合并逻辑
                    if (eventType == CanalEntry.EventType.DELETE) {
                        BasicDBObject obj = DBConvertUtil.columnToJson(rowData.getBeforeColumnsList());
                        EventData eventData = new EventData(schemaName, tableName, 3, obj);
                        dataList.add(eventData);
                        addEventToMap(tableName, eventData);
                    } else if (eventType == CanalEntry.EventType.INSERT) {
                        BasicDBObject obj = DBConvertUtil.columnToJson(rowData.getAfterColumnsList());
                        EventData eventData = new EventData(schemaName, tableName, 1, obj);
                        dataList.add(eventData);
                        addEventToMap(tableName, eventData);
                    } else if (eventType == CanalEntry.EventType.UPDATE) {
                        BasicDBObject obj = DBConvertUtil.columnToJson(rowData.getAfterColumnsList());
                        EventData eventData = new EventData(schemaName, tableName, 2, obj);
                        dataList.add(eventData);
                        addEventToMap(tableName, eventData);
                    } else {
                        logger.info("未知数据变动类型：{}", eventType);
                    }
                }
            }
        }

        //处理数据
        long overData = System.currentTimeMillis();
        if (dataMap.size() > 0) {
            for (String tableName : dataMap.keySet()) {
                //有效数据计数
                int availableCount = 0;
                BulkOperations testBulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, tableName);
                List<EventData> list = dataMap.get(tableName);
                for (EventData eventData : list) {
                    if (eventData.getType() == 1) {
                        if (eventData.getDbObject().containsField("id")) {
                            DBObject o = ObjectUtils.clone(eventData.getDbObject());
                            o.put("_id", o.get("id"));
                            o.removeField("id");
                            testBulk.insert(o);
                            availableCount++;
                        } else {
                            logger.warn("unknown data structure");
                            continue;
                        }

                    } else if (eventData.getType() == 2) {
                        Query query;
                        Update update;
                        if (eventData.getDbObject().containsField("id")) {
                            DBObject o = ObjectUtils.clone(eventData.getDbObject());
                            query = new BasicQuery(new BasicDBObject("_id", o.get("id")));
                            o.removeField("id");
                            update = new BasicUpdate(new BasicDBObject("$set", o));
                            testBulk.updateOne(query, update);
                            availableCount++;
                        } else {
                            logger.warn("unknown data structure");
                            continue;
                        }

                    } else if (eventData.getType() == 3) {
                        if (eventData.getDbObject().containsField("id")) {
                            Query query = new BasicQuery(new BasicDBObject("_id", eventData.getDbObject().get("id")));
                            testBulk.remove(query);
                            availableCount++;
                        } else {
                            logger.warn("unknown data structure");
                            continue;
                        }

                    }

                }
                BulkWriteResult executeResult = testBulk.execute();
                logger.info(execute_format, availableCount, executeResult.getInsertedCount(), executeResult.getModifiedCount(), executeResult.getRemovedCount());
            }
        }
        long overNaive = System.currentTimeMillis();

        if (dataList.size() > 0) {
            for (EventData eventData : dataList) {
                SpringUtil.doEvent(eventData.getPath(), eventData.getDbObject());
            }
        }
        long overComplete = System.currentTimeMillis();
        logger.info(execute_time_format, overData - start, overNaive - overData, overComplete - overNaive);

        return true;
    }

    private void addEventToMap(String tableName, EventData eventData) {
        if (dataMap.get(tableName) == null) {
            List<EventData> list = new ArrayList<>();
            list.add(eventData);
            dataMap.put(tableName, list);
        } else {
            dataMap.get(tableName).add(eventData);
        }
    }
}
