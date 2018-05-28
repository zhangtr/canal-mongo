package com.torry.data.handler;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.torry.data.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 逐条插入数据
 *
 * @author zhangtongrui
 * @date 2017/12/12
 */
@Component("singleMessageHandler")
public class SingleMessageHandler implements MessageHandler {

    private final static Logger logger = LoggerFactory.getLogger(SingleMessageHandler.class);
    //行数据日志
    private static String row_format = "binlog[{}:{}] , name[{},{}] , eventType : {} , executeTime : {} , delay : {}ms";
    //事务入职
    private static String transaction_format = "binlog[{}:{}] , executeTime : {} , delay : {}ms";
    //数据存储耗时日志
    private static String execute_format = "name[{},{}] , eventType : {} , rows : {} consume : {}ms";
    @Autowired
    DataService dataService;

    @Override
    public boolean execute(List<CanalEntry.Entry> entrys) throws Exception {
        for (CanalEntry.Entry entry : entrys) {
            long executeTime = entry.getHeader().getExecuteTime();
            long startTime = System.currentTimeMillis();
            long delayTime = startTime - executeTime;
            //打印事务开始结束信息
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN) {
                    CanalEntry.TransactionBegin begin = null;
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
                    CanalEntry.TransactionEnd end = null;
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
                CanalEntry.RowChange rowChage = null;
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
                    dataService.drop(entry.getHeader().getTableName());
                    continue;
                } else if (eventType == CanalEntry.EventType.QUERY || rowChage.getIsDdl()) {
                    logger.info(" sql ----> " + rowChage.getSql());
                    continue;
                }
                for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                    if (eventType == CanalEntry.EventType.DELETE) {
                        dataService.delete(rowData.getBeforeColumnsList(), entry.getHeader().getSchemaName(), entry.getHeader().getTableName());
                    } else if (eventType == CanalEntry.EventType.INSERT) {
                        dataService.insert(rowData.getAfterColumnsList(), entry.getHeader().getSchemaName(), entry.getHeader().getTableName());
                    } else if (eventType == CanalEntry.EventType.UPDATE) {
                        dataService.update(rowData.getAfterColumnsList(), entry.getHeader().getSchemaName(), entry.getHeader().getTableName());
                    } else {
                        logger.info("未知数据变动类型:{}", eventType);
                    }
                }
                long consumeTime = System.currentTimeMillis() - startTime;
                logger.info(execute_format,
                        entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                        eventType, String.valueOf(rowChage.getRowDatasCount()), String.valueOf(consumeTime));
            }
        }
        return true;
    }
}
