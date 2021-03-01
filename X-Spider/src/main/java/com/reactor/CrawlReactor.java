package com.reactor;

import com.dao.MongoDao;
import com.dao.RedisDao;
import com.dao.ZkDao;
import com.entity.*;
import com.google.gson.Gson;
import com.parser.EpaperParser;
import com.parser.IndexParser;
import com.pipeline.FilterContentPipeLine;
import com.pipeline.MongoPipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.utils.ParserUtil.parserMapping;
import static com.utils.SystemInfoUtil.*;

@Component
@Slf4j
public class CrawlReactor {



    @Autowired
    private MongoDao mongoDao;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private ZkDao zkDao;

    @Autowired
    private MongoPipeline mongoPipeline;

    @Autowired
    private FilterContentPipeLine filterContentPipeLine;

    @Value("${spider.crawler.run:false}")
    private volatile boolean runState;

    @Value("${spider.crawler.app:false}")
    private boolean appState;

    static Gson gson = new Gson();


    ExecutorService executorService = Executors.newFixedThreadPool(8);
    Downloader downloader = new HttpClientDownloader();

    @PostConstruct
    void init() {
        if (appState) {
            //注册zk节点
            CrawlNode node = new CrawlNode(getHost(), getPid(), "running");
            String info = gson.toJson(node);
            if (zkDao.registerNode(info)) {
                new Thread(() -> {
                    log.info("线程{} 单节点爬虫启动.....", Thread.currentThread().getName());
                    run();
                }, "node").start();
            } else {
                log.error("爬虫节点启动注册zk失败...");
            }
        }
    }

    private PageProcessor getSpiderInstanceByTask(TaskDO task) {
        //根据不同的类型,封装好不同的爬虫实例
        PageProcessor processor = null;
        String parserType = task.getParserType();
        Class<? extends IndexParserDO> parserDOClazz = parserMapping.get(parserType);
        Object parseVo = mongoDao.findIndexParserById(task.getParserId(), parserDOClazz);
        if ("IndexParser".equals(parserType)) {
            processor = new IndexParser(task, (IndexParserDO) parseVo);
        } else if ("电子报Parser".equals(parserType)) {
            processor = new EpaperParser(task, (EpaperParserDO) parseVo);
        }
        return processor;
    }

    void run() {
        String taskId;
        while (runState && (taskId = redisDao.take()) != null) {
            TaskDO task = mongoDao.findTaskById(taskId);
            if (task != null) {
                log.info("爬虫启动:{}", task);
                PageProcessor processor = getSpiderInstanceByTask(task);

                if (processor == null) {
                    log.warn("无法匹配到正确的spider类型:{}", task);
                    continue;
                }

                executorService.submit(() -> {
                    long start = System.currentTimeMillis();


                    Spider spider = Spider.create(processor)
                            .addUrl(task.getStartUrl())
                            .addPipeline(filterContentPipeLine)
                            .addPipeline(mongoPipeline)
                            .thread(10)
                            .setDownloader(downloader);
                    spider.run();


                    long pageCount = spider.getPageCount();
                    long end = System.currentTimeMillis();

                    //上传耗费时间日志
                    CrawlLogDO crawlLogDO = new CrawlLogDO(new Date(start), end - start, task.getId(), pageCount, NODE_PID);
                    mongoDao.savaCrawlLog(crawlLogDO);
                });
            }
        }
    }
}
