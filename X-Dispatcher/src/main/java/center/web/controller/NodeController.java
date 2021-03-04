package center.web.controller;

import center.manager.Cluster;
import com.constant.ZKConstant;
import com.dao.RedisDao;
import com.entity.CrawlNode;
import com.entity.CrawlNodeInfoVO;
import com.entity.CrawlNodeInfo;
import com.entity.ResponseVO;
import com.google.gson.Gson;
import center.manager.NodeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/api/nodes")
public class NodeController {

    @Autowired
    NodeManager nodeManager;


    @Autowired
    private RedisDao redisDao;

    Gson gson = new Gson();

    @PostMapping(path = "/all")
    public ResponseVO getAllNodes() {
        List<CrawlNodeInfoVO> result = new ArrayList<>();
        Map<String, Cluster> clusters = nodeManager.getClusters();
        for (Map.Entry<String, Cluster> entry : clusters.entrySet()) {
            Map<String, CrawlNode> nodes = entry.getValue().getNodes();

            List<String> collect = nodes.keySet().stream().map(x -> "state_" + x).collect(Collectors.toList());
            List<String> states = redisDao.getValueBatch(collect);
            for (int i = 0; i < collect.size(); i++) {
                String state = states.get(i);
                CrawlNodeInfo info = gson.fromJson(state, CrawlNodeInfo.class);
                CrawlNode crawlNode = nodes.get(collect.get(i).replaceFirst("state_", ""));
                result.add(new CrawlNodeInfoVO(crawlNode, info, entry.getKey()));
            }
        }
        return new ResponseVO(result);
    }

    @PostMapping(path = "/stop")
    public ResponseVO stopNode(@RequestBody String params) {
        HashMap hashMap = gson.fromJson(params, HashMap.class);
        String nodeId = (String) hashMap.get("nodeId");
        nodeManager.sendCmdNodeStop(nodeId);
        return new ResponseVO();
    }

    @PostMapping(path = "/start")
    public ResponseVO startNode(@RequestBody String params) {
        HashMap hashMap = gson.fromJson(params, HashMap.class);
        String nodeId = (String) hashMap.get("nodeId");
        nodeManager.sendCmdNodeStart(nodeId);
        return new ResponseVO();
    }

    @PostMapping(path = "/move")
    public ResponseVO moveNode(@RequestBody String params) {
        HashMap hashMap = gson.fromJson(params, HashMap.class);
        String nodeId = (String) hashMap.get("nodeId");
        String current = (String) hashMap.get("current");
        System.out.println(current);
        String next = current.equals(ZKConstant.Spider_Cluster_Long_ROOT) ? ZKConstant.Spider_Cluster_Short_ROOT : ZKConstant.Spider_Cluster_Long_ROOT;
        nodeManager.sendCmdNodeMoveCluster(nodeId, next);
        return new ResponseVO();
    }
}
