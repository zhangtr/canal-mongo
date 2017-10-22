package com.torry.data.config.canal;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 介绍
 *
 * @author zhangtongrui
 * @date 2017/10/12
 */
@ConfigurationProperties(prefix = "canal")
public class CanalProperties {

    private String zkServers;//zookeeper 地址
    private List<String> destination;//监听instance列表

    public CanalProperties() {
    }

    public String getZkServers() {
        return zkServers;
    }

    public void setZkServers(String zkServers) {
        this.zkServers = zkServers;
    }

    public List<String> getDestination() {
        return destination;
    }

    public void setDestination(List<String> destination) {
        this.destination = destination;
    }
}
