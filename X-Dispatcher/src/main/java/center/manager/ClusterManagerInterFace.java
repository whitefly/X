package center.manager;

public interface ClusterManagerInterFace {

    void clusterStop(String clusterId);

    void ClusterStart(String clusterId);

    void moveNodeToCluster(String nodeId, String clusterId);
}
