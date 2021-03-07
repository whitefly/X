package spider.reactor;

import com.constant.RedisConstant;
import com.constant.ZKConstant;
import com.dao.MongoDao;
import com.dao.RedisDao;
import com.dao.ZkDao;
import com.google.gson.GsonBuilder;
import org.springframework.data.redis.RedisSystemException;
import spider.downloader.ChromeDownloader;
import com.entity.*;
import com.google.gson.Gson;
import spider.parser.CustomParser;
import spider.parser.EpaperParser;
import spider.parser.IndexParser;
import spider.parser.PageParser;
import spider.pipeline.MongoPipeline;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import spider.pipeline.NewsHealthPipeLine;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.utils.ParserUtil.typeAdapter;
import static com.utils.SystemInfoUtil.*;


@Slf4j
@Component
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
    private NewsHealthPipeLine newsHealthPipeLine;


    @Value("${spider.crawler.run:false}")
    private volatile boolean reactorState;

    @Getter
    @Value("${spider.crawler.app:false}")
    private volatile boolean workState;

    private volatile String taskQueue;
    private volatile String nodePath;

    Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeAdapter).create();

    private Thread reactorThread;

    ExecutorService executorService = Executors.newFixedThreadPool(8);

    //正在执行的任务
    Map<String, Spider> runningTaskMap = new ConcurrentHashMap<>();


    @Autowired
    ChromeDownloader chromeDownloader;

    Downloader baseDownloader = new HttpClientDownloader();

    @PostConstruct
    void init() {
        //默认开始只抓取短任务
        //监听短任务队列+注册短任务集群
        taskQueue = RedisConstant.DISPATCHER_SHORT_TASK_QUEUE_KEY;

        if (reactorState) {
            //注册zk节点
            CrawlNode node = new CrawlNode(getHost(), getPid());
            String info = gson.toJson(node);
            String myNodePath;
            if ((myNodePath = zkDao.registerNode2(ZKConstant.Spider_Cluster_Short_ROOT, info)) != null) {
                nodePath = myNodePath;
                log.info("zk中 {} 注册成功,path: {} 信息: {}", ZKConstant.Spider_Cluster_Short_ROOT, nodePath, info);

                reactorThread = new Thread(() -> {
                    log.info("线程{} 单节点爬虫启动.....", Thread.currentThread().getName());
                    startReactor();
                }, "node");

                reactorThread.start();
            } else {
                log.error("爬虫节点启动注册zk失败,reactor线程未启动");
            }
        } else {
            log.info("reactorState配置为false,reactor线程未启动");
        }
    }


    private static PageProcessor genPageProcessor(TaskDO task, NewsParserDO parser) {
        //根据不同的类型,封装好不同的爬虫实例
        PageProcessor processor = null;
        String parserType = task.getParserType();
        if ("IndexParser".equals(parserType)) {
            processor = new IndexParser(task, (IndexParserDO) parser);
        } else if ("电子报Parser".equals(parserType)) {
            processor = new EpaperParser(task, (EpaperParserDO) parser);
        } else if ("PageParser".equals(parserType)) {
            processor = new PageParser(task, (PageParserDO) parser);
        } else if ("CustomParser".equals(parserType)) {
            processor = new CustomParser(task, (CustomParserDO) parser);
        }
        return processor;
    }


    void startReactor() {
        log.info("监听任务队列: {}", taskQueue);
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
            log.warn("原地休眠时被唤醒...");
        }
    }

    void doWhenWork() {
        //监控redis上的某个任务队列

        try {

            String taskEditVO = redisDao.take(taskQueue);
            TaskEditVO taskEditVO1 = gson.fromJson(taskEditVO, TaskEditVO.class);
            TaskDO task = taskEditVO1.getTask();
            NewsParserDO parser = taskEditVO1.getParser();
            PageProcessor processor = genPageProcessor(task, parser);
            log.info("获取即将启动爬虫的基本信息:{}", task);

            if (processor == null) {
                log.warn("无法匹配到正确的spider类型:[{}]", task.getParserType());
                return;
            }

            //优化点:若在redis发现访问过了,就直接跳过;
            boolean flag = true;
            if (processor instanceof IndexParser) {
                if (flag) {
                    ((IndexParser) processor).setRedisDao(redisDao);
                }
            }

            executorService.submit(() -> runCrawlTask(task, processor));

        } catch (RedisSystemException e) {
            log.warn("上一轮redis队列监听中断,现在监听队列:{}", taskQueue);
            Thread.interrupted();
        }
    }

    private void runCrawlTask(TaskDO task, PageProcessor processor) {
        long start = System.currentTimeMillis();


        CallbackSpider spider = (CallbackSpider) CallbackSpider.create(processor)
                .addUrl(task.getStartUrl())
                .addPipeline(newsHealthPipeLine)
                .addPipeline(mongoPipeline)
                .thread(10)
                .setDownloader(task.isDynamic() ? chromeDownloader : baseDownloader);

        spider.setActionWhenStart(() -> {
            //管理信息加入任务
            runningTaskMap.put(task.getId(), spider);
            log.info("task管理集合 添加任务:{} {}", task.getName(), task.getId());
        });

        spider.setActionWhenStop(() -> {
            //删除管理信息
            String id = task.getId();
            // TODO: 2021/3/6 如果因为错误导致函数没执行,spider数据一直在,怎么办?
            //若没有执行,则说明spider就没有退出.退出必然会执行这个
            if (runningTaskMap.containsKey(id)) {
                runningTaskMap.remove(id);
                log.info("task管理集合 删除任务:{} {}", task.getName(), task.getId());
            } else {
                log.error("task管理集合 没发现应该出现的任务信息,出现bug...{} {}", task.getName(), task.getId());
            }
        });

        spider.run();
        long pageCount = spider.getPageCount();
        long end = System.currentTimeMillis();

        //上传耗费时间日志
        CrawlLogDO crawlLogDO = new CrawlLogDO(new Date(start), end - start, task.getId(), pageCount, NODE_PID);
        mongoDao.savaCrawlLog(crawlLogDO);
    }

    public void setWorkState(boolean expect) {
        workState = expect;
        reactorThread.interrupt();
        log.info("workState 设置为:" + workState);
    }


    public void changeTaskQueue(String queueKey) {
        taskQueue = queueKey;
        reactorThread.interrupt();
        log.info("修改监听任务队列: {}", queueKey);

    }

    public void moveNode(String clusterRoot) {
        //zk节点删除
        CrawlNode node = new CrawlNode(getHost(), getPid());
        String info = gson.toJson(node);
        zkDao.deleteNode(nodePath);
        log.info("删除zk节点:{}", nodePath);

        //zk节点注册
        nodePath = zkDao.registerNode2(clusterRoot, info);
        log.info("注册zk节点:{}", nodePath);
    }

    public List<String> tasksOnRunning() {
        return new ArrayList<>(runningTaskMap.keySet());
    }

    public void stopTask(String taskId) {
        Spider spider = runningTaskMap.get(taskId);
        if (spider != null) {
            log.info("找到对应的任务,正在关闭:{}", taskId);
            spider.stop();
        } else {
            log.warn("未找到对应的任务:{}", taskId);
        }
    }
}
