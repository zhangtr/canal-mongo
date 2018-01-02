package com.torry.data.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClientException;
import com.mongodb.MongoSocketException;
import com.torry.data.common.EventData;
import com.torry.data.common.NameConst;
import com.torry.data.config.canal.CanalProperties;
import com.torry.data.util.DBConvertUtil;
import com.torry.data.util.SpringUtil;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

/**
 * DataService,数据处理集合
 *
 * @author zhangtongrui
 * @date 2017/10/13
 */
@Service
public class DataService {
    protected final static Logger logger = LoggerFactory.getLogger(DataService.class);

    @Resource
    MongoTemplate naiveMongoTemplate;
    @Resource
    MongoTemplate completeMongoTemplate;
    @Autowired
    CanalProperties properties;

    public void insert(List<CanalEntry.Column> data, String schemaName, String tableName) {
        DBObject obj = DBConvertUtil.columnToJson(data);
        logger.debug("insert ：{}", obj.toString());
        //订单库单独处理
        if (schemaName.equals("order")) {
            //保存原始数据
            if (tableName.startsWith("order_base_info")) {
                tableName = "order_base_info";
            } else if (tableName.startsWith("order_detail_info")) {
                tableName = "order_detail_info";
            } else {
                logger.info("unknown data ：{}.{}:{}", schemaName, tableName, obj);
                return;
            }
            insertData(schemaName, tableName, obj, obj);
        } else {
            DBObject newObj = (DBObject) ObjectUtils.clone(obj);
            if (newObj.containsField("id")) {
                newObj.put("_id", newObj.get("id"));
                newObj.removeField("id");
            }
            insertData(schemaName, tableName, newObj, obj);
        }
    }


    public void delete(List<CanalEntry.Column> data, String schemaName, String tableName) {
        DBObject obj = DBConvertUtil.columnToJson(data);
        logger.debug("delete：{}", obj.toString());
        if (schemaName.equals("order")) {
            logger.info("not support delete：{}.{}:{}", schemaName, tableName, obj);
        } else {
            deleteData(schemaName, tableName, obj);
        }
    }


    public void update(List<CanalEntry.Column> data, String schemaName, String tableName) {
        DBObject obj = DBConvertUtil.columnToJson(data);
        logger.debug("update：{}", obj.toString());
        //订单库单独处理
        if (schemaName.equals("order")) {
            if (tableName.startsWith("order_base_info")) {
                tableName = "order_base_info";
            } else if (tableName.startsWith("order_detail_info")) {
                tableName = "order_detail_info";
            } else {
                logger.info("unknown data：{}.{}:{}", schemaName, tableName, obj);
            }
            updateData(schemaName, tableName, new BasicDBObject("orderId", obj.get("orderId")), obj);
        } else {
            if (obj.containsField("id")) {
                updateData(schemaName, tableName, new BasicDBObject("_id", obj.get("id")), obj);
            } else {
                logger.info("unknown data structure");
            }
        }
    }

    public void drop(String tableName) {
        List<String> enableList = properties.queryDropEnableTableList();
        if (properties.isDropEnable() || (enableList != null && enableList.size() > 0 && enableList.contains(tableName))) {
            logger.warn("drop table {} from naive", tableName);
            System.out.println("drop table " + tableName + " from naive");
            //防止多表一起drop
            String[] names = tableName.split(",|`|'");
            for (String name : names) {
                if (StringUtils.isNotBlank(name)) {
                    naiveMongoTemplate.getCollection(name.trim()).remove(new BasicDBObject());
                }
            }
        }
    }

    public void insertData(String schemaName, String tableName, DBObject naive, DBObject complete) {
        int i = 0;
        DBObject logObj = (DBObject) ObjectUtils.clone(complete);
        //保存原始数据
        try {
            String path = "/" + schemaName + "/" + tableName + "/" + CanalEntry.EventType.INSERT.getNumber();
            i++;
            naiveMongoTemplate.getCollection(tableName).insert(naive);
            i++;
            SpringUtil.doEvent(path, complete);
            i++;
        } catch (MongoClientException | MongoSocketException clientException) {
            //客户端连接异常抛出，阻塞同步，防止mongodb宕机
            throw clientException;
        } catch (Exception e) {
            logError(schemaName, tableName, 1, i, logObj, e);
        }
    }

    public void updateData(String schemaName, String tableName, DBObject query, DBObject obj) {
        String path = "/" + schemaName + "/" + tableName + "/" + CanalEntry.EventType.UPDATE.getNumber();
        int i = 0;
        DBObject newObj = (DBObject) ObjectUtils.clone(obj);
        DBObject logObj = (DBObject) ObjectUtils.clone(obj);
        //保存原始数据
        try {
            obj.removeField("id");
            i++;
            naiveMongoTemplate.getCollection(tableName).update(query, obj);
            i++;
            SpringUtil.doEvent(path, newObj);
            i++;
        } catch (MongoClientException | MongoSocketException clientException) {
            //客户端连接异常抛出，阻塞同步，防止mongodb宕机
            throw clientException;
        } catch (Exception e) {
            logError(schemaName, tableName, 2, i, logObj, e);
        }
    }


    public void deleteData(String schemaName, String tableName, DBObject obj) {
        int i = 0;
        String path = "/" + schemaName + "/" + tableName + "/" + CanalEntry.EventType.DELETE.getNumber();
        DBObject newObj = (DBObject) ObjectUtils.clone(obj);
        DBObject logObj = (DBObject) ObjectUtils.clone(obj);
        //保存原始数据
        try {
            i++;
            if (obj.containsField("id")) {
                naiveMongoTemplate.getCollection(tableName).remove(new BasicDBObject("_id", obj.get("id")));
            }
            i++;
            SpringUtil.doEvent(path, newObj);
        } catch (MongoClientException | MongoSocketException clientException) {
            //客户端连接异常抛出，阻塞同步，防止mongodb宕机
            throw clientException;
        } catch (Exception e) {
            logError(schemaName, tableName, 3, i, logObj, e);
        }
    }

    /**
     * 记录拼接表错误记录
     *
     * @param schemaName
     * @param tableName
     * @param event      1:INSERT;2:UPDATE;3:DELETE
     * @param step       0:预处理数据错误;1:保存原始数据错误;2:保存组合数据错误，
     * @param obj
     * @param e
     */
    private void logError(String schemaName, String tableName, int event, int step, DBObject obj, Exception e) {
        logger.error("error data：name[{},{}] , eventType : {} , data : [{}]", schemaName, tableName, event, obj);
        DBObject errObj = new BasicDBObject();
        errObj.put("schemaName", schemaName);
        errObj.put("tableName", tableName);
        errObj.put("event", event);
        errObj.put("data", obj);
        errObj.put("step", step);
        errObj.put("time", new Date());
        errObj.put("error", e.toString());
        completeMongoTemplate.getCollection(NameConst.C_ERROR_RECORD).insert(errObj);
    }

    @Async("myTaskAsyncPool")
    public Future<Integer> doAsyncTask(String tableName, List<EventData> dataList, String destination) {
        try {
            MDC.put("destination", destination);
            logger.info("thread: " + Thread.currentThread().getName() + " is doing job :" + tableName);
            for (EventData eventData : dataList) {
                SpringUtil.doEvent(eventData.getPath(), eventData.getDbObject());
            }
        } catch (Exception e) {
            logger.error("thread:" + Thread.currentThread().getName() + " get Exception", e);
            return new AsyncResult(0);
        }
        return new AsyncResult(1);
    }

}
