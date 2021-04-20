package center.web.service;

import center.dispatch.Dispatcher;
import center.exception.WebException;
import center.manager.ClusterManager;
import center.utils.DynamicUtil;
import center.utils.TaskCheckerUtil;
import com.constant.QueueForTask;
import com.constant.RedisConstant;
import com.dao.MongoDao;
import com.dao.RedisDao;
import com.entity.*;
import com.mytype.ParserDOType;
import com.utils.TaskUtil;
import com.utils.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import spider.myenum.CrawlParserType;
import spider.parser.NewsParser;
import spider.parser.TestBodyParser;
import spider.parser.TestEpaperParser;
import spider.pipeline.NothingPipeline;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static center.exception.ErrorCode.*;

@Service
@Slf4j
public class TaskService {

    @Autowired
    private MongoDao mongoDao;

    @Autowired
    private Dispatcher dispatcher;

    @Autowired
    private LogService crawlService;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private DocService docService;


    public void addTask(TaskDO task, NewsParserDO parserConfig) {
        //检查重复,验证cron
        //加入mongo
        //暂时不启动
        task.setId(null);
        parserConfig.setId(null);
        task.setActive(false);

        checkTaskInfo(task, true);
        checkParserInfo(parserConfig, true);

        task.setOpDate(new Date());
        mongoDao.saveNewsParser(parserConfig);
        task.setParserId(parserConfig.getId());
        mongoDao.saveTask(task);
        log.info("新建任务成功:{}", task);

        AuditDO audit = new AuditDO("task", "create", "业务需要", task.getId(), new Date());
        mongoDao.saveAudit(audit);
    }


    public void updateTask(TaskDO task, NewsParserDO parserConfig) {
        //更新数据库
        //更新Dispatcher,如果Dispatcher不存在,则跳过,否则重新载入
        checkTaskInfo(task, false);

        checkParserInfo(parserConfig, false);

        //重新覆盖存入
        task.setActive(false);
        task.setOpDate(new Date());
        mongoDao.updateTask(task);
        mongoDao.updateNewsParser(parserConfig);

        //删除Dispatcher的定时任务
        dispatcher.delTask(task);
        log.info("更新任务成功");

        AuditDO audit = new AuditDO("task", "update", "业务需要", task.getId(), new Date());
        mongoDao.saveAudit(audit);
    }

    public TaskDO findTask(String taskId) {
        return mongoDao.findTaskById(taskId);
    }

    public List<TaskDO> findTasksByIds(List<String> ids) {
        return mongoDao.findTaskByIds(ids);
    }

    public List<TaskDO> taskDOList() {
        return mongoDao.findAllTask();
    }

    public NewsParserDO findNewsParser(String parserId) {
        return mongoDao.findNewsParserById(parserId);
    }


    public void removeTask(String taskId) {
        //删除任务,停止dispatcher,抓取结果暂时不删除
        TaskDO task = existTask(taskId);

        //删除采集任务配置
        mongoDao.delTask(task);
        mongoDao.delNewsParser(task.getParserId());
        dispatcher.delTask(task);

        //删除新闻和指纹
        docService.clearDoc(taskId);


        log.info("删除任务[success]:{}", task);

        AuditDO audit = new AuditDO("task", "del", "业务需要", task.getId(), new Date());
        mongoDao.saveAudit(audit);
    }

    public void stopTask(String taskId) {
        //修改db+dispatcher
        TaskDO task = existTask(taskId);

        dispatcher.delTask(task);
        task.setOpDate(new Date());
        task.setActive(false);
        mongoDao.updateTask(task);

        // TODO: 2020/12/22 考虑用监听者模式重构,后续可能需要添加功能
        AuditDO audit = new AuditDO("task", "stop", "业务需要", task.getId(), new Date());
        mongoDao.saveAudit(audit);
    }

    public void startTask(String taskId) {
        //判定存在
        TaskDO task = existTask(taskId);

        //修改db+添加分配器
        dispatcher.cronTask(task, task.getCron());
        task.setActive(true);
        task.setOpDate(new Date());
        mongoDao.updateTask(task);

        AuditDO audit = new AuditDO("task", "start", "业务需要", task.getId(), new Date());
        mongoDao.saveAudit(audit);
    }

    public void temporaryStart(String taskId) {
        //临时启动一次
        TaskDO task = existTask(taskId);

        String fullCrawData = dispatcher.genFullCrawlData(task);
        String queueName = QueueForTask.queueForTask.get(task.getParserType().name());
        if (queueName == null) queueName = RedisConstant.DISPATCHER_SHORT_TASK_QUEUE_KEY;

        //发送任务消息
        redisDao.put(queueName, fullCrawData);

        //日志记录
        log.info("time: {}  push taskId :{}   name:{} to {}", LocalTime.now(), taskId, task.getName(), queueName);
        DispatchLogDO dispatchLogDO = new DispatchLogDO(task.getName(), taskId, Date.from(Instant.now()));
        dispatchLogDO.setExtra("临时启动");
        mongoDao.saveDispatchLog(dispatchLogDO);
    }


    public List<TaskDO> getTasks(Integer pageIndex, Integer pageSize, String keyword, String parseType) {
        List<TaskDO> all = mongoDao.findTasksByPageIndex(pageIndex, pageSize, keyword, parseType);

        //查询任务的上次启动时间
        List<String> taskIds = all.stream().map(TaskDO::getId).collect(Collectors.toList());
        Map<String, CrawlLogDO> lastCrawlInfo = crawlService.getLastCrawlInfo(taskIds);
        all.forEach(task -> {
            if (lastCrawlInfo.get(task.getId()) != null) {
                //防止任务从未启动的情况
                task.setLastRun(lastCrawlInfo.get(task.getId()).getStartTime());
            }
        });

        //查询运行在哪个节点上
        all.forEach(task -> task.setRunHost(clusterManager.getNodeByTaskId(task.getId())));
        return all;
    }

    public long getTaskCount(String taskName, String parseType) {
        return mongoDao.taskCount(taskName, parseType);
    }

    private void checkParserInfo(NewsParserDO parser, boolean create) {
        if (!create) existIndexParser(parser.getId());

        TaskCheckerUtil.checkParserInfo(parser);

    }

    private void checkTaskInfo(TaskDO task, boolean create) {
        //保证task是存在的
        TaskDO old = null;
        if (!create) old = existTask(task.getId());

        //  验证必须的参数是否完整
        if (!TaskCheckerUtil.checkParamNotEmpty(
                task.getName(),
                task.getStartUrl(),
                task.getCron(),
                task.getParserType()
        )) {
            throw new WebException(SERVICE_TASK_CREATE_MISS_PARAM);
        }

        //判断更新后的任务是否名字和其他任务有冲突
        String taskUrl = task.getStartUrl();
        List<TaskDO> otherUrl = mongoDao.findTaskByUrl(task.getStartUrl(), task.getName());

        //去掉本身的任务
        if (!create) {
            TaskDO finalOld = old;
            otherUrl = otherUrl.stream().filter(item -> !item.getId().equals(finalOld.getId())).collect(Collectors.toList());
        }

        //验证task的名字和url是否重复
        if (!CollectionUtils.isEmpty(otherUrl)) {
            if (otherUrl.stream().anyMatch(x -> taskUrl.equals(x.getName()))) {
                throw new WebException(SERVICE_TASK_CREATE_NAME_DUP);
            } else throw new WebException(SERVICE_TASK_CREATE_URL_DUP);
        }

        //验证task的cron是否规范
        if (!CronExpression.isValidExpression(task.getCron())) {
            throw new WebException(SERVICE_TASK_CRON_INVALID);
        }

        //验证url是否规范
        if (!UrlUtil.checkUrl(task.getStartUrl()) && ParserDOType.电子报Parser != task.getParserType()) {
            throw new WebException(SERVICE_TASK_URL_INVALID);
        }

        //验证电子版的模板url是否规范
        if (ParserDOType.电子报Parser == task.getParserType()) {
            String startUrl = task.getStartUrl();
            if (!TaskUtil.isEpaperStartUrlValid(startUrl)) {
                throw new WebException(SERVICE_TASK_PAPER_URL_INVALID);
            }
        }
    }


    private TaskDO existTask(String taskId) {
        Assert.notNull(taskId, "taskId is null ! !");
        TaskDO taskById = mongoDao.findTaskById(taskId);
        if (taskById == null) throw new WebException(SERVICE_TASK_NOT_EXIST);
        return taskById;
    }

    private NewsParserDO existIndexParser(String parserId) {
        Assert.notNull(parserId, "parserId is null !");
        NewsParserDO parser = mongoDao.findNewsParserById(parserId);
        if (parser == null) throw new WebException(SERVICE_PARSER_NOT_EXIST);
        return parser;
    }

    public TestInfo testParser(TaskDO task, NewsParserDO parserBO) {
        checkParserInfo(parserBO, true);
        //统一生成test类
        PageProcessor processor;
        ParserDOType parserDOType = task.getParserType();
        CrawlParserType crawlParserType = CrawlParserType.valueOf(parserDOType.name());
        Class<? extends NewsParser> testParser = crawlParserType.getTestParser();

        //反射生成类
        try {
            Constructor<? extends NewsParser> constructor = testParser.getConstructor(TaskDO.class, parserDOType.getClazz(), TestInfo.class);
            TestInfo testInfo = new TestInfo();
            processor = constructor.newInstance(task, parserBO, testInfo);
            log.info("测试解析器信息: {}", processor);
            String startUrl = handleSpecial(processor, task);

            Spider app = Spider.create(processor).addUrl(startUrl).addPipeline(new NothingPipeline()).thread(1);
            return executeTest(task, testInfo, app);
        } catch (NoSuchMethodException e) {
            log.error("无法创建通过反射进行实例化..", e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String handleSpecial(PageProcessor parser, TaskDO task) {
        if (parser instanceof TestEpaperParser) {
            String startUrl = task.getStartUrl();
            if (!TaskUtil.isEpaperStartUrlValid(startUrl)) throw new WebException(SERVICE_TASK_PAPER_URL_INVALID);
            String url = TaskUtil.genEpaperUrl(startUrl);
            ((TestEpaperParser) parser).setFirstUrl(url);
            return url;
        } else {
            return task.getStartUrl();
        }
    }


    public Map<String, Object> testBody(TaskDO task, NewsParserDO indexParserBO, String targetUrl) {
        checkParserInfo(indexParserBO, true);

        Map<String, Object> rnt = new HashMap<>();
        TestBodyParser spider = new TestBodyParser(task, indexParserBO, rnt);
        Spider app = Spider.create(spider).addUrl(targetUrl).thread(1).addPipeline(new NothingPipeline());
        if (task.isDynamic()) {
            app.setDownloader(DynamicUtil.dynamicDownloader);
        }
        app.run();
        return rnt;
    }


    private TestInfo executeTest(TaskDO task, TestInfo testInfo, Spider app) {
        if (task.isDynamic()) {
            app.setDownloader(DynamicUtil.dynamicDownloader);
        }
        app.run();
        return testInfo;
    }
}
