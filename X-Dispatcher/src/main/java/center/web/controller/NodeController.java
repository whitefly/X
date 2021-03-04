package center.web.controller;

import com.entity.CrawlNode;
import com.entity.CrawlNodeInfoVO;
import com.entity.CrawlNodeInfo;
import com.entity.ResponseVO;
import com.google.gson.Gson;
import center.manager.NodeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

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
    private StringRedisTemplate redisTemplate;

    Gson gson = new Gson();

    @PostMapping(path = "/all")
    public ResponseVO getAllNodes() {
        Map<String, CrawlNode> nodes = nodeManager.getNodes();


        //查询对应状态
        List<String> collect = nodes.keySet().stream().map(x -> "state_" + x).collect(Collectors.toList());
        ValueOperations<String, String> op = redisTemplate.opsForValue();
        List<String> states = op.multiGet(collect);


        List<CrawlNodeInfoVO> result = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            String state = states.get(i);
            CrawlNodeInfo crawlNodeState = gson.fromJson(state, CrawlNodeInfo.class);
            CrawlNode crawlNode = nodes.get(collect.get(i).replaceFirst("state_", ""));
            result.add(new CrawlNodeInfoVO(crawlNode, crawlNodeState));
        }

        return new ResponseVO(result);
    }

    @PostMapping(path = "/stop")
    public ResponseVO stopNode(@RequestBody String params) {
        HashMap hashMap = gson.fromJson(params, HashMap.class);
        String nodeId = (String) hashMap.get("nodeId");
        nodeManager.stop(nodeId);
        return new ResponseVO();
    }

    @PostMapping(path = "/start")
    public ResponseVO startNode(@RequestBody String params) {
        HashMap hashMap = gson.fromJson(params, HashMap.class);
        String nodeId = (String) hashMap.get("nodeId");
        nodeManager.start(nodeId);
        return new ResponseVO();
    }
}
