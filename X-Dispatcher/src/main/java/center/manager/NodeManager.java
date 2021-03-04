package center.manager;

import com.constant.CmdType;
import com.dao.RedisDao;
import com.entity.CrawlNode;
import com.google.gson.Gson;
import com.constant.ZKConstant;
import com.constant.RedisConstant;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    RedisDao redisDao;


    private static final Gson gson = new Gson();

    @Getter
    Map<String, Cluster> clusters = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            //手机集群信息
            Cluster clusterShort = new Cluster(ZKConstant.Spider_Cluster_Short_ROOT);
            Cluster clusterLong = new Cluster(ZKConstant.Spider_Cluster_Long_ROOT);

            clusters.put(clusterShort.getClusterId(), clusterShort);
            clusters.put(clusterLong.getClusterId(), clusterLong);

            clusters.forEach((k, v) -> initClusterData(v));
        } catch (Exception e) {
            if (needMonitor) {
                log.error("监控启动失败...进程退出", e);
            }
        }
    }

    private void initClusterData(Cluster cluster) {
        String clusterId = cluster.getClusterId();
        Stat stat = null;
        try {
            stat = zkClient.checkExists().forPath(clusterId);
            if (stat == null) {
                //创建根节点
                zkClient.create().forPath(cluster.getClusterId());
            } else {
                //提取已经存在的节点
                Map<String, CrawlNode> nodes = cluster.nodes;
                List<CrawlNode> all = findAllNodes();
                all.forEach(x -> nodes.put(x.getId(), x));
            }

            //解析直接子节点的变化
            startWatch(clusterId);
        } catch (Exception e) {
            e.printStackTrace();
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
    public void sendCmdNodeStop(String nodeId) {
        String cmdStr = CmdType.genCmdStr(CmdType.Node_Work_Stop, null);
        redisDao.put(RedisConstant.getCmdKey(nodeId), cmdStr);
        log.info("向 {} 发送指令: {}", nodeId, cmdStr);
    }

    @Override
    public void sendCmdNodeStart(String nodeId) {
        String cmdStr = CmdType.genCmdStr(CmdType.Node_Work_Start, null);
        redisDao.put(RedisConstant.getCmdKey(nodeId), cmdStr);
        log.info("向 {} 发送指令: {}", nodeId, cmdStr);
    }

    @Override
    public void sendCmdNodeKill(String nodeId) {
        String cmdStr = CmdType.genCmdStr(CmdType.Node_Process_Kill, null);
        redisDao.put(RedisConstant.getCmdKey(nodeId), cmdStr);
        log.info("向 {} 发送指令: {}", nodeId, cmdStr);
    }

    @Override
    public void sendCmdNodeMoveCluster(String nodeId, String clusterId) {
        String cmdStr = CmdType.genCmdStr(CmdType.Node_Cluster_Move, clusterId);
        redisDao.put(RedisConstant.getCmdKey(nodeId), cmdStr);
        log.info("向 {} 发送指令: {}", nodeId, cmdStr);
    }

    private void startWatch(String clusterId) {
        CuratorCacheListener listener = CuratorCacheListener
                .builder()
                .forPathChildrenCache(clusterId, zkClient, (client, event) -> {
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            handleChildAdd(event);
                            break;
                        case CHILD_REMOVED:
                            handleChildDel(event);
                    }
                }).build();

        CuratorCache cache = CuratorCache.builder(zkClient, clusterId).build();
        cache.listenable().addListener(listener);
        cache.start();
        log.info("开始监听zk节点目录: {}", clusterId);
    }

    private void handleChildAdd(PathChildrenCacheEvent event) {
        CrawlNode crawlNode = parseInfo(event.getData());
        String path = event.getData().getPath();
        log.info(crawlNode.getId() + ":上线了");

        Cluster ownCluster = getClusterByPath(path);
        if (ownCluster != null) {
            ownCluster.nodes.put(crawlNode.getId(), crawlNode);
        } else {
            log.warn("该节点路径不在管理之内 path:{}", path);
        }

    }

    private void handleChildDel(PathChildrenCacheEvent event) {
        CrawlNode crawlNode = parseInfo(event.getData());
        String path = event.getData().getPath();
        log.info(crawlNode.getId() + ":下线了");

        Cluster ownCluster = getClusterByPath(path);
        if (ownCluster != null) {
            ownCluster.nodes.remove(crawlNode.getId());
        } else {
            log.warn("该节点路径不在管理之内 path:{}", path);
        }
        clearNodeRedisInfo(crawlNode);
    }

    private void clearNodeRedisInfo(CrawlNode crawlNode) {
        //删除zk上的命令节点和状态节点
        redisDao.deleteKey(RedisConstant.getCmdKey(crawlNode.getId()));
        redisDao.deleteKey(RedisConstant.getStateKey(crawlNode.getId()));
    }


    private CrawlNode parseInfo(ChildData data) {
        String info = new String(data.getData());
        return gson.fromJson(info, CrawlNode.class);
    }

    private Cluster getClusterByPath(String path) {
        for (Map.Entry<String, Cluster> entry : clusters.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
