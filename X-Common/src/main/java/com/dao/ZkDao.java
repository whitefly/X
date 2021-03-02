package com.dao;

import com.utils.ZKConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ZkDao {


    @Autowired
    private CuratorFramework zkClient;


    public boolean registerNode(String info) {
        //注册临时节点
        String path = ZKConstant.ZK_SPIDER_ROOT + "/node-";
        try {
            if (info == null) {
                zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path);
            } else {
                zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, info.getBytes());
            }
            return true;
        } catch (Exception e) {
            log.error("注册抓取节点失败", e);
            return false;
        }
    }

}
