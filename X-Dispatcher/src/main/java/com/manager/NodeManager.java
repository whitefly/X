package com.manager;

import com.entity.CrawlNode;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Component
public class NodeManager implements Manager {

    /**
     * 主要负责所有爬虫节点的查看,启动/暂停,关闭
     */

    @Value("${zk.spider.path}")
    private String SpiderPath;

    @Value("${zk.needmonitor:false}")
    private boolean needMonitor;

    @Autowired
    private CuratorFramework zkClient;

    @Getter
    Set<CrawlNode> nodes = new HashSet<>();

    private static Gson gson = new Gson();

    @PostConstruct
    public void init() {
        //判断节点是否存在
        try {
            Stat stat = zkClient.checkExists().forPath(SpiderPath);
            if (stat == null) {
                //创建根节点
                zkClient.create().forPath(SpiderPath);
            } else {
                //提取已经存在的节点
                List<CrawlNode> all = all();
                nodes.addAll(all);
            }

            //解析直接子节点的变化
            watch();
        } catch (Exception e) {
            if (needMonitor) {
                log.error("监控启动失败...进程退出", e);
            }
        }
    }


    @Override
    public List<CrawlNode> all() {
        List<CrawlNode> nodes = new ArrayList<>();
        try {
            List<String> nodeKeys = zkClient.getChildren().forPath(SpiderPath);
            if (nodeKeys != null) {
                for (String key : nodeKeys) {
                    byte[] bytes = zkClient.getData().forPath(key);
                    String status = new String(bytes);
                    String[] split = key.split(":");
                    String host = split[0];
                    Integer pid = Integer.parseInt(split[1]);

                    CrawlNode crawlNode = new CrawlNode(host, pid, status);
                    nodes.add(crawlNode);
                }
            }
        } catch (Exception e) {
            log.error("查询zk的CrawlNode信息失败", e);
        }
        return nodes;
    }

    @Override
    public void stop(CrawlNode node) {

    }

    @Override
    public void exit(CrawlNode node) {

    }

    @Override
    public void start(CrawlNode node) {

    }

    public void watch() {
        CuratorCacheListener listener = CuratorCacheListener
                .builder()
                .forPathChildrenCache(SpiderPath, zkClient, (client, event) -> {
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            handleChildAdd(event);
                            break;
                        case CHILD_REMOVED:
                            handleChildDel(event);
                    }
                }).build();

        CuratorCache cache = CuratorCache.builder(zkClient, SpiderPath).build();
        cache.listenable().addListener(listener);
        cache.start();
        log.info("管理节点开始监听zk...");
    }

    private void handleChildAdd(PathChildrenCacheEvent event) {
        CrawlNode crawlNode = parseInfo(event.getData());
        log.info(crawlNode.getHost() + ":" + crawlNode.getPid() + "上线");
        nodes.add(crawlNode);
    }

    private void handleChildDel(PathChildrenCacheEvent event) {
        CrawlNode crawlNode = parseInfo(event.getData());
        log.info(crawlNode.getHost() + ":" + crawlNode.getPid() + " 下线");
        nodes.remove(crawlNode);
    }

    private CrawlNode parseInfo(ChildData data) {
        String info = new String(data.getData());
        return gson.fromJson(info, CrawlNode.class);
    }
}
