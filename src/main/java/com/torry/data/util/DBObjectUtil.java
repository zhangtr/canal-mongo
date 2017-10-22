package com.torry.data.util;

import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 介绍
 *
 * @author zhangtongrui
 * @date 2017/10/16
 */
public class DBObjectUtil {

    /**
     * 从列表中删除已存在的数据
     *
     * @param list
     * @param obj
     * @return
     */
    public static List<DBObject> removeDBObject(List<DBObject> list, DBObject obj) {
        if (list == null || list.size() < 1) {
            return null;
        }
        int existFlag = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get("id").equals(obj.get("id"))) {
                existFlag = i;
            }
        }
        if (existFlag >= 0) {
            list.remove(existFlag);
        }
        return list;
    }

    /**
     * 从列表中替换已存在的数据
     *
     * @param list
     * @param obj
     * @return
     */
    public static List<DBObject> upsertDBObject(List<DBObject> list, DBObject obj) {
        if (list == null) {
            //列表不存在，添加
            list = new ArrayList<>();
            list.add(obj);
        } else {
            //列表存在，找id相同数据，有数据替换，没有数据添加
            int existFlag = -1;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).get("id").equals(obj.get("id"))) {
                    existFlag = i;
                }
            }
            if (existFlag >= 0) {
                list.remove(existFlag);
            }
            list.add(obj);
        }
        return list;
    }
}
