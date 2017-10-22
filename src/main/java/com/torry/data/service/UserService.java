package com.torry.data.service;


import com.alibaba.otter.canal.protocol.CanalEntry;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.torry.data.config.Schema;
import com.torry.data.config.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.annotation.Resource;

/**
 * 商户系统接口
 *
 * @author
 */
@Schema("demo_user")
public class UserService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    MongoTemplate completeMongoTemplate;

    @Table(value = "user_info", event = {CanalEntry.EventType.INSERT, CanalEntry.EventType.UPDATE})
    public void saveUser_UserInfo(DBObject userInfo) {
        String userNo = userInfo.get("user_no") == null ? null : userInfo.get("user_no").toString();

        DBCollection collection = completeMongoTemplate.getCollection("user");
        DBObject queryObject = new BasicDBObject("user_no", userNo);
        DBObject user = collection.findOne(queryObject);
        if (user == null) {
            user = new BasicDBObject();
            user.put("user_no", userNo);
            user.put("userInfo", userInfo);
            collection.insert(user);
        } else {
            DBObject updateObj = new BasicDBObject("userInfo", userInfo);
            DBObject update = new BasicDBObject("$set", updateObj);
            collection.update(queryObject, update);
        }
    }

    @Table(value = "user_other_info", event = {CanalEntry.EventType.INSERT, CanalEntry.EventType.UPDATE})
    public void saveUser_UserOtherInfo(DBObject userOtherInfo) {
        String userNo = userOtherInfo.get("user_no") == null ? null : userOtherInfo.get("user_no").toString();

        DBCollection collection = completeMongoTemplate.getCollection("user");
        DBObject queryObject = new BasicDBObject("user_no", userNo);
        DBObject user = collection.findOne(queryObject);
        if (user == null) {
            user = new BasicDBObject();
            user.put("user_no", userNo);
            user.put("userOtherInfo", userOtherInfo);
            collection.insert(user);
        } else {
            DBObject updateObj = new BasicDBObject("userOtherInfo", userOtherInfo);
            DBObject update = new BasicDBObject("$set", updateObj);
            collection.update(queryObject, update);
        }
    }


}
