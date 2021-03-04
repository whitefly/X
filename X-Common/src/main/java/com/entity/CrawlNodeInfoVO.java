package com.entity;

import lombok.Data;

@Data
public class CrawlNodeInfoVO {
    CrawlNode node;
    CrawlNodeInfo state;
    String cluster;

    public CrawlNodeInfoVO(CrawlNode node, CrawlNodeInfo state, String cluster) {
        this.node = node;
        this.state = state;
        this.cluster = cluster;
    }
}
