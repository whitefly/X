package com.dao;

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


    public String registerNode(String clusterRoot, String info) {
        String path = clusterRoot + "/node-";
        String newNodePath;
        try {

            if (info == null) {
                newNodePath = zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path);
            } else {
                newNodePath = zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, info.getBytes());
            }
            return newNodePath;
        } catch (Exception e) {
            log.error("注册集群节点失败", e);
        }
        return null;
    }

    public boolean deleteNode(String nodePath) {
        try {
            zkClient.delete().forPath(nodePath);
            return true;
        } catch (Exception e) {
            log.error("删除zk节点失败 path:" + nodePath, e);
        }
        return false;
    }
}
