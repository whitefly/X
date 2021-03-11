package center.web.controller;

import center.manager.NodeManager;
import center.web.service.DocService;
import center.web.service.TaskService;
import com.entity.*;
import com.google.gson.Gson;
import center.web.service.LogService;
import com.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

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


    @PostMapping(path = "/crawlLog")
    public ResponseVO crawlLogList(@RequestBody String params) {
        HashMap hashMap = GsonUtil.fromJson(params, HashMap.class);
        String taskId = (String) hashMap.get("taskId");

        List<CrawlLogDO> crawlLogDO = crawlService.getCrawlLogDO(taskId);
        return new ResponseVO(crawlLogDO);
    }

    @PostMapping(path = "/dispatchLog")
    public ResponseVO dispatchLogList(@RequestBody String params) {
        HashMap hashMap = GsonUtil.fromJson(params, HashMap.class);
        String taskId = (String) hashMap.get("taskId");

        List<DispatchLogDO> dispatchLog = crawlService.getDispatchLog(taskId);
        return new ResponseVO(dispatchLog);
    }


}
