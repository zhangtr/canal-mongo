package com.torry.data.config.mongo;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author ZQ
 * @date 2017年8月7日
 */
@ConfigurationProperties(prefix = "mongodb")
public class MultipleMongoProperties {

    private MongoProperties naive = new MongoProperties();
    private MongoProperties complete = new MongoProperties();


    public MongoProperties getNaive() {
        return naive;
    }

    public void setNaive(MongoProperties naive) {
        this.naive = naive;
    }

    public MongoProperties getComplete() {
        return complete;
    }

    public void setComplete(MongoProperties complete) {
        this.complete = complete;
    }
}