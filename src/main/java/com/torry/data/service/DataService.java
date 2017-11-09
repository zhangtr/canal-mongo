package com.torry.data.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.torry.data.common.NameConst;
import com.torry.data.util.DBConvertUtil;
import com.torry.data.util.SpringUtil;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
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
    @Resource
    MongoTemplate completeMongoTemplate;

    public void insert(List<CanalEntry.Column> data, String schemaName, String tableName) {
        DBObject obj = DBConvertUtil.columnToJson(data);
        logger.debug("insert ：{}", obj.toString());
        //分库分表单独处理
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
            //数据id作为mongodb数据主键
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
        //个性化设置
        if (schemaName.equals("order")) {
            logger.info("订单表不支持删除：{}.{}:{}", schemaName, tableName, obj);
        } else {
            deleteData(schemaName, tableName, obj);
        }
    }


    public void update(List<CanalEntry.Column> data, String schemaName, String tableName) {
        DBObject obj = DBConvertUtil.columnToJson(data);
        logger.debug("update：{}", obj.toString());
        //分库分表单独处理
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

    public void insertData(String schemaName, String tableName, DBObject naive, DBObject complete) {
        DBObject logObj = (DBObject) ObjectUtils.clone(complete);
        String path = "/" + schemaName + "/" + tableName + "/" + CanalEntry.EventType.INSERT.getNumber();
        //保存原始数据
        try {
            naiveMongoTemplate.getCollection(tableName).insert(naive);
        } catch (Exception e) {
            logNaiveError(schemaName, tableName, "INSERT", logObj, e);
        }
        //保存关联数据
        try {
            SpringUtil.doEvent(path, complete);
        } catch (Exception e) {
            logCompleteError(schemaName, tableName, "INSERT", logObj, e);
        }
    }

    public void updateData(String schemaName, String tableName, DBObject query, DBObject obj) {
        DBObject newObj = (DBObject) ObjectUtils.clone(obj);
        DBObject logObj = (DBObject) ObjectUtils.clone(obj);
        //保存原始数据
        try {
            obj.removeField("id");
            naiveMongoTemplate.getCollection(tableName).update(query, obj);
        } catch (Exception e) {
            logNaiveError(schemaName, tableName, "UPDATE", logObj, e);
        }
        String path = "/" + schemaName + "/" + tableName + "/" + CanalEntry.EventType.UPDATE.getNumber();
        //保存关联数据
        try {
            SpringUtil.doEvent(path, newObj);
        } catch (Exception e) {
            logCompleteError(schemaName, tableName, "UPDATE", logObj, e);
        }

    }


    public void deleteData(String schemaName, String tableName, DBObject obj) {
        String path = "/" + schemaName + "/" + tableName + "/" + CanalEntry.EventType.DELETE.getNumber();
        DBObject newObj = (DBObject) ObjectUtils.clone(obj);
        DBObject logObj = (DBObject) ObjectUtils.clone(obj);
        //保存原始数据
        try {
            if (obj.containsField("id")) {
                naiveMongoTemplate.getCollection(tableName).remove(new BasicDBObject("_id", obj.get("id")));
            }
        } catch (Exception e) {
            logNaiveError(schemaName, tableName, "DELETE", logObj, e);
        }
        //保存关联数据
        try {
            SpringUtil.doEvent(path, newObj);
        } catch (Exception e) {
            logCompleteError(schemaName, tableName, "DELETE", logObj, e);
        }
    }

    private void logNaiveError(String schemaName, String tableName, String event, DBObject obj, Exception e) {
        logger.error("error data：name[{},{}] , eventType : {} , data : [{}]", schemaName, tableName, event, obj);
        logger.error("保存原始数据异常:", e);
        DBObject errObj = new BasicDBObject();
        errObj.put("schemaName", schemaName);
        errObj.put("tableName", tableName);
        errObj.put("event", event);
        errObj.put("data", obj);
        errObj.put("time", new Date());
        errObj.put("error", e.toString());
        completeMongoTemplate.getCollection(NameConst.N_ERROR_RECORD).insert(errObj);
    }

    /**
     * 记录拼接表错误记录
     *
     * @param schemaName
     * @param tableName
     * @param event
     * @param obj
     * @param e
     */
    private void logCompleteError(String schemaName, String tableName, String event, DBObject obj, Exception e) {
        logger.error("error data：name[{},{}] , eventType : {} , data : [{}]", schemaName, tableName, event, obj);
        logger.error("保存组合数据异常:", e);
        DBObject errObj = new BasicDBObject();
        errObj.put("schemaName", schemaName);
        errObj.put("tableName", tableName);
        errObj.put("event", event);
        errObj.put("data", obj);
        errObj.put("time", new Date());
        errObj.put("error", e.toString());
        completeMongoTemplate.getCollection(NameConst.C_ERROR_RECORD).insert(errObj);
    }


}
