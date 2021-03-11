package center.manager;

import center.exception.NodeEventUnKnownException;
import com.constant.ZKConstant;
import com.entity.Cluster;
import com.entity.CrawlNode;
import com.entity.CrawlNodeInfo;
import com.google.gson.Gson;
import com.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class ClusterManager {
    @Autowired
    private CuratorFramework zkClient;

    @Value("${spider.cluster.manage:false}")
    private boolean needMonitor;

    @Autowired
    NodeManager nodeManager;

    private final Map<String, Cluster> clusterMap = new ConcurrentHashMap<>();

    //这个状态会经常更新
    private Map<CrawlNode, CrawlNodeInfo> nodeStateMap;

    private Map<String, String> task2NodeMap;


    @PostConstruct
    void init() {
        try {
            //手机集群信息
            Cluster clusterShort = new Cluster(ZKConstant.Spider_Cluster_Short_ROOT);
            Cluster clusterLong = new Cluster(ZKConstant.Spider_Cluster_Long_ROOT);

            clusterMap.put(clusterShort.getClusterId(), clusterShort);
            clusterMap.put(clusterLong.getClusterId(), clusterLong);

            //若目录已有数据,收集
            clusterMap.forEach((k, v) -> initClusterDataFromZK(v));

            //监控
            clusterMap.forEach((k, v) -> startWatchTargetPath(k));
        } catch (Exception e) {
            if (needMonitor) {
                log.error("监控启动失败...进程退出", e);

            }
        }
    }


    private void initClusterDataFromZK(Cluster cluster) {
        String clusterId = cluster.getClusterId();
        Stat stat;
        try {
            stat = zkClient.checkExists().forPath(clusterId);
            if (stat == null) {
                //创建根节点
                zkClient.create().forPath(cluster.getClusterId());
            } else {
                //提取已经存在的节点
                List<CrawlNode> all = findNodesOnPath(clusterId);
                all.forEach(cluster::addNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startWatchTargetPath(String clusterId) {
        CuratorCacheListener listener = CuratorCacheListener
                .builder()
                .forPathChildrenCache(clusterId, zkClient, (client, event) -> {
                    try {
                        switch (event.getType()) {
                            case CHILD_ADDED:
                                handleChildAddOnCluster(event);
                                break;
                            case CHILD_REMOVED:
                                handleChildDelOnCluster(event);
                                break;
                        }
                    } catch (NodeEventUnKnownException e) {
                        log.error("该消息不属于管理范围:{}", event.getData().getPath());
                    }
                }).build();

        CuratorCache cache = CuratorCache.builder(zkClient, clusterId).build();
        cache.listenable().addListener(listener);
        cache.start();
        log.info("开始监听zk节点目录: {}", clusterId);
    }

    private List<CrawlNode> findNodesOnPath(String path) {
        List<CrawlNode> nodes = new ArrayList<>();
        try {
            List<String> nodeName = zkClient.getChildren().forPath(path);
            if (nodeName != null) {
                for (String key : nodeName) {
                    byte[] bytes = zkClient.getData().forPath(path + "/" + key);
                    String base = new String(bytes);
                    CrawlNode crawlNode = GsonUtil.fromJson(base, CrawlNode.class);
                    nodes.add(crawlNode);
                }
            }
        } catch (Exception e) {
            log.error("查询zk的CrawlNode信息失败:" + path, e);
        }
        return nodes;
    }

    private void handleChildAddOnCluster(PathChildrenCacheEvent event) throws NodeEventUnKnownException {
        ChildData zkNode = event.getData();
        CrawlNode crawlNode = nodeManager.handleChildAdd(zkNode.getData());
        Cluster ownCluster = getClusterByPath(zkNode.getPath());
        ownCluster.addNode(crawlNode);
    }

    private void handleChildDelOnCluster(PathChildrenCacheEvent event) throws NodeEventUnKnownException {
        ChildData zkNode = event.getData();
        CrawlNode crawlNode = nodeManager.handleChildDel(zkNode.getData());
        Cluster ownCluster = getClusterByPath(zkNode.getPath());
        ownCluster.removeNode(crawlNode);
        if (nodeStateMap != null) {
            nodeStateMap.remove(crawlNode);
        }
    }

    private Cluster getClusterByPath(String path) throws NodeEventUnKnownException {
        //此时path是一个完整的子节点路径
        for (Map.Entry<String, Cluster> entry : clusterMap.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        throw new NodeEventUnKnownException(path);
    }

    @Scheduled(cron = "*/2 * * * * ?")
    private void fetchNodeState() {
        List<CrawlNode> allNodes = new ArrayList<>();
        for (Cluster c : clusterMap.values()) {
            allNodes.addAll(c.getNodes());
        }
        nodeStateMap = nodeManager.fetchBatchCrawlInfos(allNodes);


        //分析节点状态
        Map<String, String> taskMap = new HashMap<>();
        nodeStateMap.forEach((node, v) -> {
            if (v != null) {
                List<String> taskIds = v.getRunTasks();
                taskIds.forEach(id -> taskMap.put(id, node.getId()));
            }


        });
        task2NodeMap = taskMap;
    }

    public Collection<Cluster> getClusters() {
        return clusterMap.values();
    }

    public CrawlNodeInfo getState(CrawlNode crawlNode) {
        if (nodeStateMap != null) {
            return nodeStateMap.get(crawlNode);
        }
        return null;
    }

    public int getNodeCount() {
        Collection<Cluster> clusters = clusterMap.values();
        int count = 0;
        for (Cluster c : clusters) {
            int size = c.getNodes().size();
            count += size;
        }
        return count;
    }

    public String getNodeByTaskId(String taskId) {
        if (task2NodeMap != null) {
            String s = task2NodeMap.get(taskId);
            return s != null ? s : "";
        }
        return "";
    }
}
