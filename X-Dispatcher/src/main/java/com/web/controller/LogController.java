package com.web.controller;

import com.entity.CrawlLogDO;
import com.entity.DispatchLogDO;
import com.entity.ResponseVO;
import com.google.gson.Gson;
import com.web.service.LogService;
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
}
