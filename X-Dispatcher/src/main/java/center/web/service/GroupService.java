package center.web.service;


import center.exception.ErrorCode;
import center.exception.WebException;
import com.constant.RedisConstant;
import com.dao.MongoDao;
import com.dao.RedisDao;
import com.entity.SubscribeGroupDO;
import com.entity.TaskDO;
import com.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GroupService {

    @Autowired
    MongoDao mongoDao;

    @Autowired
    RedisDao redisDao;

    final static String userId = "admin";

    @PostConstruct
    private void init() {
        //将MongoDB的全部组映射到Redis中
        List<SubscribeGroupDO> all = mongoDao.listGroups();

        // TODO: 2021/4/19 循环太多,后续想办法进行batch操作
        for (SubscribeGroupDO group : all) {
            List<String> keywords = group.getKeywords();
            List<String> tasks = group.getTasks();
            String keywordsJson = GsonUtil.toJson(keywords);
            for (String taskId : tasks) {
                String subscribeKey = RedisConstant.getSubscribeKey(taskId);
                redisDao.addMap(subscribeKey, group.getId(), keywordsJson);
            }
            log.info("订阅组[ {} ]加载至redis成功", group.getGroupName());
        }
    }

    public void createGroup(String name) {
        SubscribeGroupDO subscribeGroupDO = new SubscribeGroupDO();
        subscribeGroupDO.setUserId(userId);
        subscribeGroupDO.setActive(false);
        subscribeGroupDO.setGroupName(name);
        subscribeGroupDO.setTasks(new ArrayList<>());
        subscribeGroupDO.setKeywords(new ArrayList<>());
        mongoDao.insertGroup(subscribeGroupDO);
    }

    public List<SubscribeGroupDO> groupList() {
        return mongoDao.listGroups();
    }

    public void deleteGroup(String groupId) {
        //删除mongo
        SubscribeGroupDO group;
        //检查订阅组是否存在
        if (groupId == null || (group = mongoDao.findGroupById(groupId)) == null) {
            throw new WebException(ErrorCode.SERVICE_SUBSCRIBE_NOT_EXIST);
        }

        List<String> taskIds = group.getTasks();
        mongoDao.deleteGroup(groupId);
        log.info("删除mongoDB的组成功: groupId[{}]", groupId);

        //删除Redis中的数据
        for (String taskId : taskIds) {
            String subscribeKey = RedisConstant.getSubscribeKey(taskId);
            redisDao.delMap(subscribeKey, group.getId());
        }
        log.info("在Redis任务 {} 中删除订阅组[ {} ]", taskIds, groupId);
    }


    public void addTaskToGroup(String groupId, List<String> taskIds) {
        SubscribeGroupDO group = checkAndGetGroup(groupId);

        //打印出未添加的任务
        Set<String> old = new HashSet<>(group.getTasks());
        List<TaskDO> taskByIds = mongoDao.findTaskByIds(taskIds);
        Set<String> plus = taskByIds.stream().map(TaskDO::getId).collect(Collectors.toSet());
        plus.removeAll(old);
        log.info("订阅组Group[ {} ]新订阅的任务为: {}", group.getId(), plus);
        group.getTasks().addAll(plus);
        mongoDao.updateGroup(group);

        //增加redis中的信息
        for (String taskId : plus) {
            String subscribeKey = RedisConstant.getSubscribeKey(taskId);
            List<String> keywords = group.getKeywords();
            redisDao.addMap(subscribeKey, groupId, GsonUtil.toJson(keywords));
        }
    }

    public void updateKeywords(String groupId, List<String> keywords) {
        //修改MongoDB
        SubscribeGroupDO group = checkAndGetGroup(groupId);
        List<String> oldKeywords = group.getKeywords();
        group.setKeywords(keywords);
        mongoDao.updateGroup(group);
        log.info("MongoDB 更新订阅组Group[ {} ] 触发词成功: old:{}  new:{}", group.getId(), oldKeywords, keywords);

        //修改Redis
        List<String> tasks = group.getTasks();
        for (String taskId : tasks) {
            String subscribeKey = RedisConstant.getSubscribeKey(taskId);
            redisDao.addMap(subscribeKey, group.getId(), GsonUtil.toJson(keywords));
        }
        log.info("Redis订阅组[ {} ]每个任务更新触发词成功: {}", group.getId(), tasks);
    }


    public void removeTaskFromGroup(String groupId, String taskId) {
        SubscribeGroupDO group = checkAndGetGroup(groupId);
        group.getTasks().remove(taskId);
        mongoDao.updateGroup(group);

        //删除redis中的信息
        String subscribeKey = RedisConstant.getSubscribeKey(taskId);
        redisDao.delMap(subscribeKey, groupId);
    }

    public void changeActive(String groupId) {
        SubscribeGroupDO group = checkAndGetGroup(groupId);
        Boolean active = group.getActive();
        group.setActive(!active);
        mongoDao.updateGroup(group);
    }


    private SubscribeGroupDO checkAndGetGroup(String groupId) {
        SubscribeGroupDO group;
        //检查订阅组是否存在
        if (groupId == null || (group = mongoDao.findGroupById(groupId)) == null) {
            throw new WebException(ErrorCode.SERVICE_SUBSCRIBE_NOT_EXIST);
        }
        return group;
    }

    private TaskDO checkAndGetTask(String taskId) {
        //检查taskId是否存在
        TaskDO task;
        if (taskId == null || (task = mongoDao.findTaskById(taskId)) == null) {
            throw new WebException(ErrorCode.SERVICE_TASK_NOT_EXIST);
        }
        return task;
    }

    private SubscribeGroupDO checkAllAndGetGroup(String groupId, String taskId) {
        SubscribeGroupDO group = checkAndGetGroup(groupId);
        TaskDO taskDO = checkAndGetTask(taskId);

        //任务是否已经被加入到订阅组中
        List<String> tasks = group.getTasks();
        if (tasks.contains(taskDO.getId())) {
            throw new WebException(ErrorCode.SERVICE_SUBSCRIBE_DUP_TASK);
        }
        return group;
    }


}
