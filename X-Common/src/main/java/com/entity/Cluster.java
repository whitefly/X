package com.entity;

import lombok.Data;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Data
public class Cluster {

    //集群id(就是zk路径)
    String ClusterId;

    //集群下管理的节点(key为 host:process)
    private Map<String, CrawlNode> nodes = new ConcurrentHashMap<>();

    public Cluster(String clusterId) {
        ClusterId = clusterId;
    }

    public void addNode(CrawlNode node) {
        if (node != null) {
            nodes.put(node.getId(), node);
        }
    }

    public int getNodeSize() {
        return nodes.size();
    }

    public void removeNode(CrawlNode node) {
        nodes.remove(node.getId());
    }

    public Collection<CrawlNode> getNodes() {
        return nodes.values();
    }

}
