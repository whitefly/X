package center.manager;

public interface Manager {


    void sendCmdNodeStop(String nodeId);

    void sendCmdNodeStart(String nodeId);

    void sendCmdNodeMoveCluster(String nodeId, String clusterId);
}
