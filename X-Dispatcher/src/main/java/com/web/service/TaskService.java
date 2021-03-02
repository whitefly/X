package com.web.service;

import com.dao.MongoDao;
import com.dao.RedisDao;
import com.dispatch.Dispatcher;
import com.entity.*;
import com.exception.WebException;
import com.parser.TestBodyParser;
import com.parser.TestIndexParser;
import com.utils.FieldUtil;
import com.utils.RedisConstant;
import com.utils.UrlUtil;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Spider;

import java.util.*;
import java.util.stream.Collectors;

import static com.exception.ErrorCode.*;
import static com.utils.ChromeUtil.chromeDownloader;

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


    public void addTask(TaskDO task, IndexParserDO indexParser) {
        //检查重复,验证cron
        //加入mongo
        //暂时不启动
        task.setId(null);
        indexParser.setId(null);
        task.setActive(false);

        checkTaskInfo(task, true);
        checkParserInfo(indexParser, true);

        mongoDao.saveIndexParser(indexParser);
        task.setParserId(indexParser.getId());
        mongoDao.saveTask(task);
        log.info("新建任务成功:{}", task);

        AuditDO audit = new AuditDO("task", "create", "业务需要", task.getId(), new Date());
        mongoDao.saveAudit(audit);
    }


    public void updateTask(TaskDO task, IndexParserDO indexParser) {
        //更新数据库
        //更新Dispatcher,如果Dispatcher不存在,则跳过,否则重新载入
        checkTaskInfo(task, false);
        checkParserInfo(indexParser, false);

        //重新覆盖存入
        task.setActive(false);
        mongoDao.updateTask(task);
        mongoDao.updateIndexParser(indexParser);

        //删除Dispatcher的定时任务
        dispatcher.delTask(task);
        log.info("更新任务成功");

        AuditDO audit = new AuditDO("task", "update", "业务需要", task.getId(), new Date());
        mongoDao.saveAudit(audit);
    }

    public TaskDO findTask(String taskId) {
        return mongoDao.findTaskById(taskId);
    }

    public IndexParserDO findIndexParser(String parserId) {
        return mongoDao.findIndexParserById(parserId);
    }


    public void removeTask(String taskId) {
        //删除任务,停止dispatcher,抓取结果暂时不删除
        TaskDO task = existTask(taskId);

        mongoDao.delTask(task);
        mongoDao.delIndexParser(task.getParserId());
        dispatcher.delTask(task);

        //删除redis中的指纹set
        String hashKey = RedisConstant.getHashKey(taskId);
        redisDao.delSet(hashKey);
        log.info("删除redis set:" + hashKey);

        log.info("删除任务[success]:{}", task);

        AuditDO audit = new AuditDO("task", "del", "业务需要", task.getId(), new Date());
        mongoDao.saveAudit(audit);
    }

    public void stopTask(String taskId) {
        //修改db+dispatcher
        TaskDO task = existTask(taskId);

        dispatcher.delTask(task);
        mongoDao.updateTaskState(task, false);

        // TODO: 2020/12/22 考虑用监听者模式重构,后续可能需要添加功能
        AuditDO audit = new AuditDO("task", "stop", "业务需要", task.getId(), new Date());
        mongoDao.saveAudit(audit);
    }

    public void startTask(String taskId) {
        //判定存在
        TaskDO task = existTask(taskId);

        //修改db+添加分配器
        dispatcher.cronTask(task, task.getCron());
        mongoDao.updateTaskState(task, true);

        AuditDO audit = new AuditDO("task", "start", "业务需要", task.getId(), new Date());
        mongoDao.saveAudit(audit);
    }

    public List<TaskDO> getTasks() {
        List<TaskDO> all = mongoDao.findAll();

        //设置上次启动时间
        List<String> taskIds = all.stream().map(TaskDO::getId).collect(Collectors.toList());
        Map<String, CrawlLogDO> lastCrawlInfo = crawlService.getLastCrawlInfo(taskIds);
        all.forEach(task -> {
            if (lastCrawlInfo.get(task.getId()) != null) {
                //防止任务从未启动的情况
                task.setLastRun(lastCrawlInfo.get(task.getId()).getStartTime());
            }
        });
        return all;
    }

    private void checkParserInfo(IndexParserDO parser, boolean create) {
        if (!create) existIndexParser(parser.getId());

        //允许必要字段的xpath不存在,会进入到自动解析流程
        if (!FieldUtil.checkParam(
                parser.getIndexRule()
//                parser.getTitleRule(),
//                parser.getContentRule()
        )) {
            throw new WebException(SERVICE_PARSER_MISS_MUST_FIELD);
        }

        //验证extra的格式
        checkExtra(parser.getExtra());
    }

    private void checkExtra(List<FieldDO> fields) {
        if (!CollectionUtils.isEmpty(fields)) return;

        //name必须有,css| xpath | re | special  必须有一个
        for (FieldDO f : fields) {
            if (f.getName() == null) throw new WebException(SERVICE_PARSER_MISS_FIELD_NAME);
            if (!FieldUtil.checkParam(f.getCss(), f.getXpath(), f.getSpecial(), f.getRe()))
                throw new WebException(SERVICE_PARSER_MISS_FIELD_VALUE);
        }

        //验证是否有重名
        Set<String> set = fields.stream().map(FieldDO::getName).collect(Collectors.toSet());
        if (fields.size() != set.size()) throw new WebException(SERVICE_PARSER_FIELD_DUP);
    }


    private void checkTaskInfo(TaskDO task, boolean create) {
        //保证task是存在的
        TaskDO old = null;
        if (!create) old = existTask(task.getId());

        //  验证必须的参数是否完整
        if (!FieldUtil.checkParam(
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
        if (!UrlUtil.checkUrl(task.getStartUrl())) {
            throw new WebException(SERVICE_TASK_URL_INVALID);
        }
    }


    private TaskDO existTask(String taskId) {
        if (taskId == null) throw new ValueException("taskId is null ! ! ");
        TaskDO taskById = mongoDao.findTaskById(taskId);
        if (taskById == null) throw new WebException(SERVICE_TASK_NOT_EXIST);
        return taskById;
    }

    private IndexParserDO existIndexParser(String IndexParserId) {
        if (IndexParserId == null) throw new ValueException("IndexParserId is null !");
        IndexParserDO parser = mongoDao.findIndexParserById(IndexParserId);
        if (parser == null) throw new WebException(SERVICE_PARSER_NOT_EXIST);
        return parser;
    }

    public List<String> testIndex(TaskDO task, IndexParserDO indexParser) {
        List<String> rnt = new ArrayList<>();
        TestIndexParser spider = new TestIndexParser(task, indexParser, rnt);
        //抓取单页面
        Spider app = Spider.create(spider).addUrl(task.getStartUrl()).thread(1);
        if (task.isDynamic()) {
            app.setDownloader(chromeDownloader);
        }
        app.run();
        return rnt;
    }

    public Map<String, Object> testBody(TaskDO task, IndexParserDO indexParser, String targetUrl) {
        Map<String, Object> rnt = new HashMap<>();
        TestBodyParser spider = new TestBodyParser(task, indexParser, rnt);
        Spider.create(spider).addUrl(targetUrl).thread(1).run();
        return rnt;
    }
}
