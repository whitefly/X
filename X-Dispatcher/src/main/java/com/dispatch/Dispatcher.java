package com.dispatch;


import com.dao.MongoDao;
import com.dao.RedisDao;
import com.entity.DispatchLogDO;
import com.entity.TaskDO;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class Dispatcher {
    @Autowired
    private RedisDao redisDao;

    @Autowired
    private MongoDao mongoDao;


    @Value("${spider.dispatcher.run:false}")
    private boolean runState;


    @Getter
    @Setter
    public static class PusherJob implements Job {
        private String taskId;
        private String taskName;


        @SneakyThrows
        @Override
        public void execute(JobExecutionContext jobExecutionContext) {
            RedisDao redisDao = (RedisDao) jobExecutionContext.getScheduler().getContext().get("redisDao");
            MongoDao mongoDao = (MongoDao) jobExecutionContext.getScheduler().getContext().get("mongoDao");
            redisDao.put(taskId);


            //保存到mongoDB
            LocalTime now = LocalTime.now();
            log.info("time: {}  push taskId :{}   name:{}", now, taskId, taskName);
            DispatchLogDO dispatchLogDO = new DispatchLogDO(taskName, taskId, Date.from(Instant.now()));
            mongoDao.saveDispatchLog(dispatchLogDO);
        }
    }


    Scheduler scheduler;

    @PostConstruct
    void init() throws SchedulerException {
        if (runState) {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            //传入redisDao(令人蛋疼的方式)
            scheduler.getContext().put("redisDao", redisDao);
            scheduler.getContext().put("mongoDao", mongoDao);

            //读取所有的task,导入scheduler
            List<TaskDO> all = mongoDao.findAllTaskActive();
            all.forEach(item -> cronTask(item, item.getCron()));
            log.info("从mongoDB载入定时的激活任务成功,任务个数:{}", all.size());
            scheduler.start();
        }
    }


    public void cronTask(TaskDO task, String cron) {
        String taskJobKey = getTaskJobKey(task);
        String triggerKey = getTriggerJobKey(task);
        try {
            //设定上传任务
            JobDetail pusher = JobBuilder.newJob(PusherJob.class)
                    .withIdentity(taskJobKey)
                    .usingJobData("taskId", task.getId())
                    .usingJobData("taskName", task.getName())
                    .build();

            CronTrigger trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();

            scheduler.scheduleJob(pusher, trigger);
            log.info("schedule taskId success:cron[{}] taskId[{}]", cron, task.getId());
        } catch (ObjectAlreadyExistsException e) {
            log.info("task已经加载至scheduler:{}", task);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    public void delTask(TaskDO task) {
        if (!existCronTask(task)) {
            log.warn("Dispatcher delete task[ignore]: task[{}] not exists in dispatcher", task.getId());
            return;
        }
        String taskJobKey = getTaskJobKey(task);
        try {
            scheduler.deleteJob(JobKey.jobKey(taskJobKey));
            log.info("Dispatcher delete task[success]: {}", task);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public boolean existCronTask(TaskDO task) {
        String taskJobKey = getTaskJobKey(task);
        try {
            return scheduler.checkExists(JobKey.jobKey(taskJobKey));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getTaskJobKey(TaskDO task) {
        return "task_" + task.getId();
    }

    private String getTriggerJobKey(TaskDO task) {
        return "trigger_" + task.getId();
    }

}
