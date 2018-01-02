package com.torry.data;

import com.torry.data.config.canal.CanalProperties;
import com.torry.data.config.execute.TaskThreadPoolConfig;
import com.torry.data.config.mongo.MultipleMongoProperties;
import com.torry.data.util.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Import(value = {SpringUtil.class})
@EnableConfigurationProperties({MultipleMongoProperties.class, CanalProperties.class, TaskThreadPoolConfig.class})
@SpringBootApplication(exclude = MongoAutoConfiguration.class)
public class Application {
    @Autowired
    CanalInitHandler canalInitHandler;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Component
    class InitRunner implements CommandLineRunner {
        @Override
        public void run(String... strings) throws Exception {
            canalInitHandler.initCanalStart();
        }
    }
}
