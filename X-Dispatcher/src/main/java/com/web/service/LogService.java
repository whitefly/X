package com.web.service;

import com.dao.MongoDao;
import com.entity.CrawlLogDO;
import com.entity.DispatchLogDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LogService {

    @Autowired
    MongoDao mongoDao;

    public Map<String, CrawlLogDO> getLastCrawlInfo(List<String> taskIds) {
        List<CrawlLogDO> lastCrawlLog = mongoDao.findLastCrawlLog(taskIds);
        HashMap<String, CrawlLogDO> mapping = new HashMap<>();
        lastCrawlLog.forEach(item -> mapping.put(item.getTaskId(), item));
        return mapping;
    }

    public List<CrawlLogDO> getCrawlLogDO(String taskId) {
        List<CrawlLogDO> allCrawlLog = mongoDao.findAllCrawlLog(taskId, 1);
        return allCrawlLog;
    }

    public List<DispatchLogDO> getDispatchLog(String taskId) {
        List<DispatchLogDO> allDispatchLog = mongoDao.findAllDispatchLog(taskId, 1);
        return allDispatchLog;
    }
}
