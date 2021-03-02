package com.manager;

import com.entity.CrawlNode;
import com.google.gson.Gson;
import com.utils.ZKConstant;
import com.utils.RedisConstant;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;


@Slf4j
@Component
public class NodeManager implements Manager {

    /**
     * 主要负责所有爬虫节点的查看,启动/暂停,关闭
     */


    @Value("${zk.needmonitor:false}")
    private boolean needMonitor;

    @Autowired
    private CuratorFramework zkClient;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Getter
    Map<String, CrawlNode> nodes = new HashMap<>();

    private static final Gson gson = new Gson();

    @PostConstruct
    public void init() {
        //判断节点是否存在
        try {
            Stat stat = zkClient.checkExists().forPath(ZKConstant.ZK_SPIDER_ROOT);
            if (stat == null) {
                //创建根节点
                zkClient.create().forPath(ZKConstant.ZK_SPIDER_ROOT);
            } else {
                //提取已经存在的节点
                List<CrawlNode> all = findAllNodes();
                all.forEach(x -> nodes.put(x.getId(), x));
            }

            //解析直接子节点的变化
            startWatch();
        } catch (Exception e) {
            if (needMonitor) {
                log.error("监控启动失败...进程退出", e);
            }
        }
    }


    private List<CrawlNode> findAllNodes() {
        List<CrawlNode> nodes = new ArrayList<>();
        try {
            List<String> nodeName = zkClient.getChildren().forPath(ZKConstant.ZK_SPIDER_ROOT);
            System.out.println(nodeName);
            if (nodeName != null) {
                for (String key : nodeName) {
                    byte[] bytes = zkClient.getData().forPath(ZKConstant.ZK_SPIDER_ROOT + "/" + key);
                    String base = new String(bytes);
                    CrawlNode crawlNode = gson.fromJson(base, CrawlNode.class);
                    nodes.add(crawlNode);
                }
            }
        } catch (Exception e) {
            log.error("查询zk的CrawlNode信息失败", e);
        }
        return nodes;
    }

    @Override
    public void stop(String nodeId) {
        ListOperations<String, String> op = redisTemplate.opsForList();
        op.leftPush(RedisConstant.getCmdKey(nodeId), "stop");
    }

    @Override
    public void start(String nodeId) {
        ListOperations<String, String> op = redisTemplate.opsForList();
        op.leftPush(RedisConstant.getCmdKey(nodeId) + nodeId, "start");
    }


    public void startWatch() {
        CuratorCacheListener listener = CuratorCacheListener
                .builder()
                .forPathChildrenCache(ZKConstant.ZK_SPIDER_ROOT, zkClient, (client, event) -> {
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            handleChildAdd(event);
                            break;
                        case CHILD_REMOVED:
                            handleChildDel(event);
                    }
                }).build();

        CuratorCache cache = CuratorCache.builder(zkClient, ZKConstant.ZK_SPIDER_ROOT).build();
        cache.listenable().addListener(listener);
        cache.start();
        log.info("管理节点开始监听zk...");
    }

    private void handleChildAdd(PathChildrenCacheEvent event) {
        CrawlNode crawlNode = parseInfo(event.getData());
        log.info(crawlNode.getId() + ":上线了");
        nodes.put(crawlNode.getId(), crawlNode);
    }

    private void handleChildDel(PathChildrenCacheEvent event) {
        CrawlNode crawlNode = parseInfo(event.getData());
        log.info(crawlNode.getId() + ":下线了");
        nodes.remove(crawlNode.getId());
        clearNodeRedisInfo(crawlNode);
    }

    private void clearNodeRedisInfo(CrawlNode crawlNode) {
        //删除zk上的命令节点和状态节点
        redisTemplate.delete(RedisConstant.getCmdKey(crawlNode.getId()));
        redisTemplate.delete(RedisConstant.getStateKey(crawlNode.getId()));
    }

    private CrawlNode parseInfo(ChildData data) {
        String info = new String(data.getData());
        return gson.fromJson(info, CrawlNode.class);
    }


}
