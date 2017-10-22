package com.torry.data.config.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MultipleMongoConfig {

    @Autowired
    private MultipleMongoProperties mongoProperties;

    @Primary
    @Bean(name = "naiveMongoTemplate")
    public MongoTemplate naiveMongoTemplate() throws Exception {
        MappingMongoConverter converter =
                new MappingMongoConverter(new DefaultDbRefResolver(naiveFactory()), new MongoMappingContext());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(naiveFactory(), converter);
    }

    @Bean
    @Qualifier("completeMongoTemplate")
    public MongoTemplate completeMongoTemplate() throws Exception {
        MappingMongoConverter converter =
                new MappingMongoConverter(new DefaultDbRefResolver(completeFactory()), new MongoMappingContext());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(completeFactory(), converter);
    }

    @Bean
    @Primary
    public MongoDbFactory naiveFactory() throws Exception {
        MongoClient client = new MongoClient(new MongoClientURI(mongoProperties.getNaive().getUri()));
        return new SimpleMongoDbFactory(client, mongoProperties.getNaive().getDatabase());
    }

    @Bean
    public MongoDbFactory completeFactory() throws Exception {
        MongoClient client = new MongoClient(new MongoClientURI(mongoProperties.getComplete().getUri()));
        return new SimpleMongoDbFactory(client, mongoProperties.getComplete().getDatabase());
    }
}

