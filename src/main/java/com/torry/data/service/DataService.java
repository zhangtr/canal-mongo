package com.torry.data.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.torry.data.util.DBConvertUtil;
import com.torry.data.util.SpringUtil;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 介绍
 *
 * @author zhangtongrui
 * @date 2017/10/13
 */
@Service
public class DataService {
    protected final static Logger logger = LoggerFactory.getLogger(DataService.class);

    @Resource
    MongoTemplate naiveMongoTemplate;


    public void insert(List<CanalEntry.Column> data, String schemaName, String tableName) {
        DBObject obj = DBConvertUtil.columnToJson(data);
        logger.info("insert ：{}", obj.toString());

        //订单库单独处理
        if (schemaName.equals("p4_order")) {
            //保存原始数据
            if (tableName.startsWith("p4_order_base_info")) {
                tableName = "p4_order_base_info";
            } else if (tableName.startsWith("p4_order_detail_info")) {
                tableName = "p4_order_detail_info";
            } else {
                logger.info("unknown data ：{}.{}:{}", schemaName, tableName, obj);
                return;
            }
            //保存关联数据
            String path = "/" + schemaName + "/" + tableName + "/" + CanalEntry.EventType.INSERT.getNumber();
            //保存原始数据
            naiveMongoTemplate.getCollection(tableName).insert(obj);
            //保存关联数据
            SpringUtil.doEvent(path, obj);
        } else {
            //保存关联数据
            String path = "/" + schemaName + "/" + tableName + "/" + CanalEntry.EventType.INSERT.getNumber();
            //clone对象，方式对象被修改
            DBObject newObj = (DBObject) ObjectUtils.clone(obj);
            SpringUtil.doEvent(path, newObj);
            //保存原始数据
            if (obj.containsField("id")) {
                obj.put("_id", obj.get("id"));
                obj.removeField("id");
            }
            //catch 异常，防止重复读报错
            try {
                naiveMongoTemplate.getCollection(tableName).insert(obj);
            } catch (Exception e) {
                logger.error("insert data error ：{}", e);
            }
        }
    }


    public void delete(List<CanalEntry.Column> data, String schemaName, String tableName) {
        DBObject obj = DBConvertUtil.columnToJson(data);
        logger.info("delete：{}", obj.toString());
        if (schemaName.equals("p4_order")) {
            logger.info("订单表不支持删除：{}.{}:{}", schemaName, tableName, obj);
        } else {
            String path = "/" + schemaName + "/" + tableName + "/" + CanalEntry.EventType.DELETE.getNumber();
            SpringUtil.doEvent(path, obj);
            if (obj.containsField("id")) {
                naiveMongoTemplate.getCollection(tableName).remove(new BasicDBObject("_id", obj.get("id")));
            }
        }
    }


    public void update(List<CanalEntry.Column> data, String schemaName, String tableName) {
        DBObject obj = DBConvertUtil.columnToJson(data);
        logger.info("update：{}", obj.toString());
        //订单库单独处理
        if (schemaName.equals("p4_order")) {
            if (tableName.startsWith("p4_order_base_info")) {
                tableName = "p4_order_base_info";
            } else if (tableName.startsWith("p4_order_detail_info")) {
                tableName = "p4_order_detail_info";
            } else {
                logger.info("unknown data：{}.{}:{}", schemaName, tableName, obj);
            }
            //修改关联数据
            String path = "/" + schemaName + "/" + tableName + "/" + CanalEntry.EventType.UPDATE.getNumber();
            //修改原始数据
            naiveMongoTemplate.getCollection(tableName).update(new BasicDBObject("orderId", obj.get("orderId")), obj);
            SpringUtil.doEvent(path, obj);
        } else {
            //修改关联数据
            String path = "/" + schemaName + "/" + tableName + "/" + CanalEntry.EventType.UPDATE.getNumber();
            DBObject newObj = (DBObject) ObjectUtils.clone(obj);
            SpringUtil.doEvent(path, newObj);
            //修改原始数据
            if (obj.containsField("id")) {
                Object id = obj.get("id");
                obj.removeField("id");
                try {
                    naiveMongoTemplate.getCollection(tableName).update(new BasicDBObject("_id", id), obj);
                } catch (Exception e) {
                    logger.error("update data error ：{}", e);
                }
            } else {
                logger.info("unknown data structure");
            }
        }
    }

}
