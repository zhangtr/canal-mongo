package com.torry.data.util;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 数据转换类
 *
 * @author zhangtongrui
 * @date 2017/10/12
 */
public class DBConvertUtil {
    private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /**
     * binlog数据列转换成DBObject
     *
     * @param columns
     * @return
     */
    public static DBObject columnToJson(List<CanalEntry.Column> columns) {
        DBObject obj = new BasicDBObject();
        try {
            for (CanalEntry.Column column : columns) {
                String mysqlType = column.getMysqlType();
                //int类型，长度11以下为Integer，以上为long
                if (mysqlType.startsWith("int")) {
                    int lenBegin = mysqlType.indexOf('(');
                    int lenEnd = mysqlType.indexOf(')');
                    if (lenBegin > 0 && lenEnd > 0) {
                        int length = Integer.parseInt(mysqlType.substring(lenBegin + 1, lenEnd));
                        if (length > 10) {
                            obj.put(column.getName(), StringUtils.isBlank(column.getValue()) ? null : Long.parseLong(column.getValue()));
                            continue;
                        }
                    }
                    obj.put(column.getName(), StringUtils.isBlank(column.getValue()) ? null : Integer.parseInt(column.getValue()));
                } else if (mysqlType.startsWith("bigint")) {
                    obj.put(column.getName(), StringUtils.isBlank(column.getValue()) ? null : Long.parseLong(column.getValue()));
                } else if (mysqlType.startsWith("decimal")) {
                    int lenBegin = mysqlType.indexOf('(');
                    int lenCenter = mysqlType.indexOf(',');
                    int lenEnd = mysqlType.indexOf(')');
                    if (lenBegin > 0 && lenEnd > 0 && lenCenter > 0) {
                        int length = Integer.parseInt(mysqlType.substring(lenCenter + 1, lenEnd));
                        if (length == 0) {
                            obj.put(column.getName(), StringUtils.isBlank(column.getValue()) ? null : Long.parseLong(column.getValue()));
                            continue;
                        }
                    }
                    obj.put(column.getName(), StringUtils.isBlank(column.getValue()) ? null : Double.parseDouble(column.getValue()));
                } else if (mysqlType.equals("datetime") || mysqlType.equals("timestamp")) {
                    obj.put(column.getName(), StringUtils.isBlank(column.getValue()) ? null : DATE_TIME_FORMAT.parse(column.getValue()));
                } else if (mysqlType.equals("date")) {
                    obj.put(column.getName(), StringUtils.isBlank(column.getValue()) ? null : DATE_FORMAT.parse(column.getValue()));
                } else if (mysqlType.equals("time")) {
                    obj.put(column.getName(), StringUtils.isBlank(column.getValue()) ? null : TIME_FORMAT.parse(column.getValue()));
                } else {
                    obj.put(column.getName(), column.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
