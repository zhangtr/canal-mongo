package com.torry.data.cancal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import com.mongodb.MongoClientException;
import com.mongodb.MongoSocketException;
import com.torry.data.handler.BulkMessageHandler;
import com.torry.data.handler.MessageHandler;
import com.torry.data.handler.SingleMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.Assert;

/**
 * canalClient 启动类
 *
 * @author zhangtongri
 * @date 2017/10/10
 */

public class CanalClient {

    private final static Logger logger = LoggerFactory.getLogger(CanalClient.class);
    private volatile boolean running = false;
    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread t, Throwable e) {
            logger.error("parse events has an error", e);
        }
    };
    private Thread thread = null;
    private CanalConnector connector;
    private static String canal_get = "get_message batchId : {} , entrySize : {}";
    private static String canal_ack = "ack_message batchId : {} ";

    private String destination;

    public CanalClient(String destination, CanalConnector connector) {
        this.destination = destination;
        this.connector = connector;
    }

    public void start() {
        Assert.notNull(connector, "connector is null");
        thread = new Thread(new Runnable() {
            public void run() {
                logger.info("destination:{} running", destination);
                process();
            }
        });
        thread.setUncaughtExceptionHandler(handler);
        thread.start();
        running = true;
    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        MDC.remove("destination");
    }

    private void process() {
        int batchSize = 5 * 1024;
        while (running) {
            try {
                MDC.put("destination", destination);
                connector.connect();
                connector.subscribe();
                while (running) {
                    Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId != -1 && size > 0) {
                        logger.info(canal_get, batchId, size);

                        //优先选择批处理方式处理数据，处理失败或者处理异常转用单条数据插入方式
                        boolean isSuccess;
                        try {
                            MessageHandler messageHandler = new BulkMessageHandler(message.getEntries());
                            isSuccess = messageHandler.execute();
                        } catch (MongoClientException | MongoSocketException clientException) {
                            //客户端连接异常抛出，阻塞同步，防止mongodb宕机
                            throw clientException;
                        } catch (Exception e) {
                            logger.error("批处理方式处理数据失败", e);
                            isSuccess = false;
                        }
                        if (!isSuccess) {
                            MessageHandler messageHandler = new SingleMessageHandler(message.getEntries());
                            messageHandler.execute();
                        }
                        logger.info(canal_ack, batchId);
                    }
                    connector.ack(batchId); // 提交确认
                    // connector.rollback(batchId); // 处理失败, 回滚数据
                }
            } catch (Exception e) {
                logger.error("process error!", e);
            } finally {
                connector.disconnect();
                MDC.remove("destination");
            }
        }
    }

}
