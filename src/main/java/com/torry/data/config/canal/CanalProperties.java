package com.torry.data.config.canal;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
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
    private boolean dropEnable; //监听删除数据（drop和truncate）开关
    private String dropEnableTables;//允许删除数据（drop和truncate）的表

    private List<String> dropEnableTableList;

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

    public String getDropEnableTables() {
        return dropEnableTables;
    }

    public void setDropEnableTables(String dropEnableTables) {
        this.dropEnableTables = dropEnableTables;
        this.dropEnableTableList = Arrays.asList(dropEnableTables.split(","));
    }

    public List<String> queryDropEnableTableList() {
        return dropEnableTableList;
    }

    public boolean isDropEnable() {
        return dropEnable;
    }

    public void setDropEnable(boolean dropEnable) {
        this.dropEnable = dropEnable;
    }
}
