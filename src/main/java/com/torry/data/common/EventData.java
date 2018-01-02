package com.torry.data.common;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 介绍
 *
 * @author zhangtongrui
 * @date 2017/12/14
 */
public class EventData {
    private String schemaName;
    private String tableName;
    private int type;
    private BasicDBObject dbObject;

    public EventData() {
    }

    public EventData(String schemaName, String tableName, int type, BasicDBObject dbObject) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.type = type;
        this.dbObject = dbObject;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public DBObject getDbObject() {
        return dbObject;
    }

    public void setDbObject(BasicDBObject dbObject) {
        this.dbObject = dbObject;
    }

    public String getPath() {
        return "/" + schemaName + "/" + tableName + "/" + type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
