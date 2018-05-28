package com.torry.data.handler;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;

/**
 * 介绍
 *
 * @author zhangtongrui
 * @date 2017/12/12
 */
public interface MessageHandler {
    boolean execute(List<CanalEntry.Entry> entrys) throws Exception;
}
