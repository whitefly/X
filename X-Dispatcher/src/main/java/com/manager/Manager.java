package com.manager;

import com.entity.CrawlNode;

import java.util.List;

public interface Manager {

    List<CrawlNode> all();

    void stop(CrawlNode node);

    void exit(CrawlNode node);

    void start(CrawlNode node);
}
