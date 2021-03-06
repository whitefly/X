package center.manager;

public interface NodeManagerInterFace {


    void sendCmdNodeStop(String nodeId);

    void sendCmdNodeStart(String nodeId);

    void sendCmdNodeKill(String nodeId);

    void sendCmdNodeMove(String nodeId, String clusterId);
}
