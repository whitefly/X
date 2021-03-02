package com.reactor;

import com.dao.MongoDao;
import com.dao.RedisDao;
import com.dao.ZkDao;
import com.downloader.ChromeDownloader;
import com.entity.*;
import com.google.gson.Gson;
import com.parser.EpaperParser;
import com.parser.IndexParser;
import com.pipeline.FilterContentPipeLine;
import com.pipeline.MongoPipeline;
import lombok.Getter;
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
import static java.lang.System.exit;

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
    private volatile boolean reactorState;

    @Getter
    @Value("${spider.crawler.app:false}")
    private volatile boolean workState;

    static Gson gson = new Gson();

    private Thread reactorThread;


    ExecutorService executorService = Executors.newFixedThreadPool(8);


    @Autowired
    ChromeDownloader chromeDownloader;

    Downloader baseDownloader = new HttpClientDownloader();

    @PostConstruct
    void init() {
        if (reactorState) {
            //注册zk节点
            CrawlNode node = new CrawlNode(getHost(), getPid());
            String info = gson.toJson(node);
            if (zkDao.registerNode(info)) {
                log.info("注册成功信息为:" + info);
                reactorThread = new Thread(() -> {
                    log.info("线程{} 单节点爬虫启动.....", Thread.currentThread().getName());
                    startReactor();
                }, "node");
                reactorThread.start();
            } else {
                log.error("爬虫节点启动注册zk失败,reactor线程未启动,进程结束");
                exit(2);
            }
        } else {
            log.info("reactorState配置为false,reactor线程未启动");
        }
    }

    private PageProcessor getPageProcessorByTask(TaskDO task) {
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


    void startReactor() {
        boolean lastRound = true;
        while (reactorState) {
            if (lastRound != workState) {
                log.info(lastRound ? "reactor状态 work -----> free" : "reactor状态 free -----> work");
            }

            lastRound = workState;
            if (workState) {
                //处于运行状态(监控MQ)
                doWhenWork();
            } else {
                //处于非运行状态(定时sleep)
                doWhenFree();
            }

        }
    }

    void doWhenFree() {
        //原地休眠
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void doWhenWork() {
        //监控某个队列的端口
        String taskId = redisDao.take();

        TaskDO task = mongoDao.findTaskById(taskId);
        if (task != null) {
            log.info("获取即将启动爬虫的基本信息:{}", task);
            PageProcessor processor = getPageProcessorByTask(task);

            if (processor == null) {
                log.warn("无法匹配到正确的spider类型:[{}]", task.getParserType());
                return;
            }

            executorService.submit(() -> runCrawlTask(task, processor));
        } else {
            log.warn("无法查询到id对应的抓取任务:" + taskId);
        }
    }

    private void runCrawlTask(TaskDO task, PageProcessor processor) {
        long start = System.currentTimeMillis();
        Spider spider = Spider.create(processor)
                .addUrl(task.getStartUrl())
                .addPipeline(filterContentPipeLine)
                .addPipeline(mongoPipeline)
                .thread(10)
                .setDownloader(baseDownloader);

        //是否采用动态下载器
        Downloader down = task.isDynamic() ? chromeDownloader : baseDownloader;
        spider.setDownloader(down);
        spider.run();

        long pageCount = spider.getPageCount();
        long end = System.currentTimeMillis();

        //上传耗费时间日志
        CrawlLogDO crawlLogDO = new CrawlLogDO(new Date(start), end - start, task.getId(), pageCount, NODE_PID);
        mongoDao.savaCrawlLog(crawlLogDO);
    }

    public void stopWork() {
        workState = false;
        log.info("workState 切换为:" + workState);
    }

    public void startWork() {
        workState = true;
        log.info("workState 切换为:" + workState);
    }
}
