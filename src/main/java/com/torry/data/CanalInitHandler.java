package com.torry.data;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.torry.data.cancal.CanalClient;
import com.torry.data.config.canal.CanalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * 初始化订阅集群模式canal
 *
 * @author zhangtongrui
 * @date 2017/10/10
 */
@Component
public class CanalInitHandler {
    protected final static Logger logger = LoggerFactory.getLogger(CanalInitHandler.class);

    @Autowired
    private CanalProperties canalProperties;

    private final static List<CanalClient> canalClientList = new ArrayList<>();

    public void initCanalStart() {
        List<String> destinations = canalProperties.getDestination();
        if (destinations != null && destinations.size() > 0) {
            for (String destination : destinations) {
                logger.info("## start the canal client : {}", destination);
                // 基于zookeeper动态获取canal server的地址，建立链接，其中一台server发生crash，可以支持failover
                CanalConnector connector = CanalConnectors.newClusterConnector(canalProperties.getZkServers(), destination, "", "");
                CanalClient client = new CanalClient(destination, connector);
                client.start();
                canalClientList.add(client);
            }
        }
    }

    @PreDestroy
    public void canalStop() {
        for (CanalClient canalClient : canalClientList) {
            logger.info("## stop the canal client : {}", canalClient.getDestination());
            canalClient.stop();
        }
        try {
            Thread.currentThread().join(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("## all client stopped ");
    }
}
