package spider.reactor;

import com.constant.RedisConstant;
import com.constant.ZKConstant;
import com.dao.MongoDao;
import com.dao.RedisDao;
import com.dao.ZkDao;
import com.entity.*;
import com.mytype.CrawlType;
import com.utils.GsonUtil;
import com.utils.TaskUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.stereotype.Component;
import spider.downloader.HtmlUnitDownloader;
import spider.exception.ProcessorUnserializeError;
import spider.parser.*;
import spider.pipeline.MongoPipeline;
import spider.pipeline.NewsHealthPipeLine;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.utils.SystemInfoUtil.*;


@Slf4j
@Component
public class CrawlReactor {

    private final MongoDao mongoDao;

    private final RedisDao redisDao;

    private final ZkDao zkDao;

    private final MongoPipeline mongoPipeline;

    private final NewsHealthPipeLine newsHealthPipeLine;


    @Value("${spider.crawler.run:false}")
    private volatile boolean reactorState;


    @Getter
    @Value("${spider.crawler.app:false}")
    private volatile boolean workState;

    private volatile String taskQueue;
    private volatile String nodePath;


    private Thread reactorThread;

    ExecutorService executorService = Executors.newFixedThreadPool(8);

    //正在执行的任务
    Map<String, Spider> runningTaskMap = new ConcurrentHashMap<>();


    Downloader dynamicDownloader = new HtmlUnitDownloader();

    Downloader baseDownloader = new HttpClientDownloader();

    public CrawlReactor(MongoDao mongoDao, RedisDao redisDao, ZkDao zkDao, MongoPipeline mongoPipeline, NewsHealthPipeLine newsHealthPipeLine) {
        this.mongoDao = mongoDao;
        this.redisDao = redisDao;
        this.zkDao = zkDao;
        this.mongoPipeline = mongoPipeline;
        this.newsHealthPipeLine = newsHealthPipeLine;
    }

    @PostConstruct
    void init() {
        //默认开始只抓取短任务
        //监听短任务队列+注册短任务集群
        taskQueue = RedisConstant.DISPATCHER_SHORT_TASK_QUEUE_KEY;

        if (reactorState) {
            //注册zk节点
            String info = GsonUtil.toJson(new CrawlNode(getHost(), getPid()));

            if ((nodePath = zkDao.registerNode(ZKConstant.Spider_Cluster_Short_ROOT, info)) != null) {
                log.info("zk中 {} 注册成功,path: {} 信息: {}", ZKConstant.Spider_Cluster_Short_ROOT, nodePath, info);
                reactorThread = new Thread(this::startReactor, "reactor_thread");
                reactorThread.start();
            } else {
                log.error("爬虫节点启动注册zk失败,reactor线程未启动");
            }
        } else {
            log.info("reactorState配置为false,reactor线程未启动");
        }
    }


    private PageProcessor genPageProcessor(TaskDO task, NewsParserDO parser) throws ProcessorUnserializeError {
        //根据不同的类型,封装好不同的爬虫实例
        // TODO: 2021/3/11 后期采用反射来重构
        PageProcessor processor = null;
        CrawlType parserType = task.getParserType();
        switch (parserType) {
            case IndexParser:
                processor = new IndexParser(task, (IndexParserDO) parser);
                break;
            case 电子报Parser:
                processor = new EpaperParser(task, (EpaperParserDO) parser);
                break;
            case PageParser:
                processor = new PageParser(task, (PageParserDO) parser);
                break;
            case CustomParser:
                processor = new CustomParser(task, (CustomParserDO) parser);
                break;
            case AjaxParser:
                processor = new AjaxParser(task, (AjaxParserDO) parser);
                break;
        }

        if (processor == null) {
            throw new ProcessorUnserializeError("无法匹配对应的parserType:" + parserType);
        }

        log.info("获取即将启动任务的解析器基本信息:{}", processor);
        return processor;
    }

    private void optimizeForProcessor(PageProcessor processor) {
        //优化点:下载网页前,判断之前是否访问过(查询redis)
        if (processor instanceof IndexParser) {
            ((IndexParser) processor).setRedisDao(redisDao);
        }
    }


    void startReactor() {
        log.info("线程{} Reactor启动.....", Thread.currentThread().getName());

        log.info("监听任务队列: {}", taskQueue);
        boolean lastRound = workState;
        while (reactorState) {
            checkWorkState(lastRound);
            lastRound = workState;


            if (workState) {
                doWhenWork();
            } else {
                doWhenFree();
            }
        }
    }

    private void checkWorkState(boolean lastRound) {
        if (lastRound != workState) {
            log.info(lastRound ? "reactor状态 work -----> free" : "reactor状态 free -----> work");
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
        //监控redis任务队列
        try {
            String taskEditVOJson = redisDao.take(taskQueue);

            TaskEditVO taskEditVO = GsonUtil.fromJson(taskEditVOJson, TaskEditVO.class);
            TaskDO task = taskEditVO.getTask();
            NewsParserDO parser = taskEditVO.getParser();
            PageProcessor processor = genPageProcessor(task, parser);
            //抓取优化流程(主要为了IndexParser)
            optimizeForProcessor(processor);

            executorService.submit(() -> runSpider(task, processor));
        } catch (RedisSystemException e) {
            log.warn("上一轮redis队列监听中断,现在监听队列:{}", taskQueue);
            Thread.interrupted();
        } catch (ProcessorUnserializeError e) {
            log.warn("反序列任务错误,执行失败", e);
        }
    }


    private void runSpider(TaskDO task, PageProcessor processor) {

        //获取Spider
        HookSpider spider = genHookSpider(task, processor);

        //设置钩子函数
        setSpiderRunningHook(task, spider);

        //启动任务
        spider.run();

        //日志记录
        logCrawl(task, spider);
    }

    private void logCrawl(TaskDO task, HookSpider spider) {
        //抓取日志记录
        String extra = spider.isForceStop() ? "中途手动关闭" : null;
        long pageCount = spider.getPageCount();
        long end = System.currentTimeMillis();
        CrawlLogDO crawlLogDO = new CrawlLogDO(spider.getStartTime(), end - spider.getStartTime().getTime(), task.getId(), pageCount, NODE_PID, extra);
        mongoDao.savaCrawlLog(crawlLogDO);
    }

    private HookSpider genHookSpider(TaskDO task, PageProcessor processor) {
        HookSpider spider = (HookSpider) HookSpider.create(processor)
                .thread(10)
                .setDownloader(task.isDynamic() ? dynamicDownloader : baseDownloader)
                .addPipeline(newsHealthPipeLine)
                .addPipeline(mongoPipeline);

        //处理电子报初始url
        addFirstUrl(task, processor, spider);
        return spider;
    }

    private void addFirstUrl(TaskDO task, PageProcessor processor, HookSpider spider) {
        //主要处理电子报初始url
        CrawlType parserType = task.getParserType();
        if (CrawlType.电子报Parser == parserType) {
            //电子报的格式需要单独处理 {YYYY},{MM},{dd}
            String todayUrl = TaskUtil.genEpaperUrl(task.getStartUrl());
            spider.addUrl(todayUrl);

            if (processor instanceof EpaperParser) {
                ((EpaperParser) processor).setFirstUrl(todayUrl);
            }
            log.info("电子报执行初始url: {}", todayUrl);
        } else {
            spider.addUrl(task.getStartUrl());
        }
    }

    private void setSpiderRunningHook(TaskDO task, HookSpider spider) {
        //开始前执行(加入管理信息)
        spider.setActionWhenStart(() -> {
            runningTaskMap.put(task.getId(), spider);
            log.info("task管理集合 添加任务:{} {}", task.getName(), task.getId());
        });

        //停止后执行(删除管理信息)
        spider.setActionWhenStop(() -> {
            String id = task.getId();
            if (runningTaskMap.containsKey(id)) {
                runningTaskMap.remove(id);
                log.info("task管理集合 删除任务:{} {}", task.getName(), task.getId());
            } else {
                log.warn("task管理集合 未发现应该出现的任务信息,出现bug...{} {}", task.getName(), task.getId());
            }
        });
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
        String info = GsonUtil.toJson(node);
        zkDao.deleteNode(nodePath);
        log.info("删除zk节点:{}", nodePath);

        //zk节点注册
        nodePath = zkDao.registerNode(clusterRoot, info);
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
            //设置标志位,日志使用
            if (spider instanceof HookSpider) {
                ((HookSpider) spider).setForceStop(true);
            }
        } else {
            log.warn("未找到对应的任务:{}", taskId);
        }
    }
}
