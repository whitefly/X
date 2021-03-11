package center.web.controller;

import center.manager.ClusterManager;
import center.manager.NodeManager;
import com.constant.ZKConstant;
import com.entity.*;
import com.google.gson.Gson;
import com.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/api/nodes")
public class NodeController {

    @Autowired
    NodeManager nodeManager;

    @Autowired
    ClusterManager clusterManager;


    @PostMapping(path = "/all")
    public ResponseVO getAllNodes() {
        List<CrawlNodeInfoVO> result = new ArrayList<>();
        Collection<Cluster> clusters = clusterManager.getClusters();
        for (Cluster c : clusters) {
            Collection<CrawlNode> nodes = c.getNodes();
            for (CrawlNode node : nodes) {
                CrawlNodeInfo state = clusterManager.getState(node);
                CrawlNodeInfoVO crawlNodeInfoVO = new CrawlNodeInfoVO(node, state, c.getClusterId());
                result.add(crawlNodeInfoVO);
            }
        }
        result.sort((Comparator.comparing(o -> o.getNode().getId())));
        return new ResponseVO(result);
    }

    @PostMapping(path = "/stop")
    public ResponseVO stopNode(@RequestBody String params) {
        HashMap hashMap = GsonUtil.fromJson(params, HashMap.class);
        String nodeId = (String) hashMap.get("nodeId");
        nodeManager.sendCmdNodeStop(nodeId);
        return new ResponseVO();
    }

    @PostMapping(path = "/start")
    public ResponseVO startNode(@RequestBody String params) {
        HashMap hashMap = GsonUtil.fromJson(params, HashMap.class);
        String nodeId = (String) hashMap.get("nodeId");
        nodeManager.sendCmdNodeStart(nodeId);
        return new ResponseVO();
    }

    @PostMapping(path = "/move")
    public ResponseVO moveNode(@RequestBody String params) {
        HashMap hashMap = GsonUtil.fromJson(params, HashMap.class);
        String nodeId = (String) hashMap.get("nodeId");
        String current = (String) hashMap.get("current");
        String next = current.equals(ZKConstant.Spider_Cluster_Long_ROOT) ? ZKConstant.Spider_Cluster_Short_ROOT : ZKConstant.Spider_Cluster_Long_ROOT;
        nodeManager.sendCmdNodeMove(nodeId, next);
        return new ResponseVO();
    }

    @PostMapping(path = "/closeTask")
    public ResponseVO stopTask(@RequestBody String params) {
        HashMap hashMap = GsonUtil.fromJson(params, HashMap.class);
        String nodeId = (String) hashMap.get("nodeId");
        String taskId = (String) hashMap.get("taskId");
        System.out.println(taskId);
        nodeManager.sendCmdNodeTaskStop(nodeId, taskId);
        return new ResponseVO();
    }

    @PostMapping(path = "/kill")
    public ResponseVO killNode(@RequestBody String params) {
        HashMap hashMap = GsonUtil.fromJson(params, HashMap.class);
        String nodeId = (String) hashMap.get("nodeId");
        nodeManager.sendCmdNodeKill(nodeId);
        return new ResponseVO();
    }
}
