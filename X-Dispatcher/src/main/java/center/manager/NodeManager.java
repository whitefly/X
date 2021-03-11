package center.manager;

import com.mytype.CmdType;
import com.constant.RedisConstant;
import com.dao.RedisDao;
import com.entity.CrawlNode;
import com.entity.CrawlNodeInfo;
import com.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Component
public class NodeManager implements NodeManagerInterFace {

    /**
     * 主要负责爬虫节点的查看,启动/暂停,关闭
     */

    @Autowired
    RedisDao redisDao;


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
    public void sendCmdNodeMove(String nodeId, String clusterId) {
        String cmdStr = CmdType.genCmdStr(CmdType.Node_Cluster_Move, clusterId);
        redisDao.put(RedisConstant.getCmdKey(nodeId), cmdStr);
        log.info("向 {} 发送指令: {}", nodeId, cmdStr);
    }

    public void sendCmdNodeTaskStop(String nodeId, String taskId) {
        String cmdStr = CmdType.genCmdStr(CmdType.Node_Task_Close, taskId);
        redisDao.put(RedisConstant.getCmdKey(nodeId), cmdStr);
        log.info("向 {} 发送指令: {}", nodeId, cmdStr);
    }


    CrawlNode handleChildAdd(byte[] zkNodeData) {
        CrawlNode crawlNode = parseInfo(zkNodeData);
        log.info(crawlNode.getId() + ":上线了");
        return crawlNode;
    }

    CrawlNode handleChildDel(byte[] zkNodeData) {
        CrawlNode crawlNode = parseInfo(zkNodeData);
        log.info(crawlNode.getId() + ":下线了");
        clearNodeRedisInfo(crawlNode);
        return crawlNode;
    }

    private void clearNodeRedisInfo(CrawlNode crawlNode) {
        //删除zk上的命令节点和状态节点
        redisDao.deleteKey(RedisConstant.getCmdKey(crawlNode.getId()));
        redisDao.deleteKey(RedisConstant.getStateKey(crawlNode.getId()));
    }


    private CrawlNode parseInfo(byte[] info) {
        String s = new String(info);
        return GsonUtil.fromJson(s, CrawlNode.class);
    }


    public Map<CrawlNode, CrawlNodeInfo> fetchBatchCrawlInfos(List<CrawlNode> nodes) {
        List<String> stateKeys = nodes.stream().map(x -> RedisConstant.getStateKey(x.getId())).collect(Collectors.toList());
        List<String> states = redisDao.getValueBatch(stateKeys);
        List<CrawlNodeInfo> infos = states.stream().map(stateStr -> GsonUtil.fromJson(stateStr, CrawlNodeInfo.class)).collect(Collectors.toList());

        Map<CrawlNode, CrawlNodeInfo> result = new HashMap<>();
        int size = stateKeys.size();
        for (int i = 0; i < size; i++) {
            result.put(nodes.get(i), infos.get(i));
        }
        return result;
    }
}
