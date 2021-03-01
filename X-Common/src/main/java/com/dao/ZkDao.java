package com.dao;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;


@Component
@Slf4j
public class ZkDao {

    @Getter
    @Value("${zk.spider.path}")
    public String SpiderPath;

    @Autowired
    private CuratorFramework zkClient;


    public boolean registerNode(String info) {
        //注册临时节点
        String path = SpiderPath + "/node-";
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

    public List<String> getNodes() {
        try {
            //获取所有注册的抓取节点
            return zkClient.getChildren().forPath(SpiderPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
