package center.web.controller;

import center.manager.Cluster;
import center.manager.NodeManager;
import center.web.service.DocService;
import center.web.service.TaskService;
import com.entity.*;
import com.google.gson.Gson;
import center.web.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/api/crawl")
public class LogController {

    @Autowired
    LogService crawlService;

    @Autowired
    DocService DocService;

    @Autowired
    TaskService taskService;

    @Autowired
    NodeManager manager;


    Gson gson = new Gson();

    @PostMapping(path = "/crawlLog")
    public ResponseVO crawlLogList(@RequestBody String params) {
        HashMap hashMap = gson.fromJson(params, HashMap.class);
        String taskId = (String) hashMap.get("taskId");

        List<CrawlLogDO> crawlLogDO = crawlService.getCrawlLogDO(taskId);
        return new ResponseVO(crawlLogDO);
    }

    @PostMapping(path = "/dispatchLog")
    public ResponseVO dispatchLogList(@RequestBody String params) {
        HashMap hashMap = gson.fromJson(params, HashMap.class);
        String taskId = (String) hashMap.get("taskId");

        List<DispatchLogDO> dispatchLog = crawlService.getDispatchLog(taskId);
        return new ResponseVO(dispatchLog);
    }

    @PostMapping(path = "/dashboard")
    public ResponseVO dashboardData() {
        List<DataPlus> dataPluses = crawlService.DataPlus();

        List<String> date = new ArrayList<>();
        List<Integer> count = new ArrayList<>();
        dataPluses.forEach(x -> {
            date.add(x.getTime());
            count.add(x.getCount());
        });
        DataPlusVO dataPlusVO = new DataPlusVO(date, count);
        long newsCount = DocService.getDocCountByTaskId(null);
        long taskCount = taskService.getTaskCount();
        Map<String, Cluster> clusters = manager.getClusters();
        int nodeCount = 0;
        for (Cluster c : clusters.values()) {
            nodeCount += c.getNodes().size();
        }

        dataPlusVO.setNewsCount(newsCount);
        dataPlusVO.setTaskCount(taskCount);
        dataPlusVO.setNodeCount((long) nodeCount);
        return new ResponseVO(dataPlusVO);
    }
}
