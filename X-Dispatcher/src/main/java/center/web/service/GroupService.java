package center.web.service;


import com.constant.RedisConstant;
import com.dao.MongoDao;
import com.dao.RedisDao;
import com.entity.SubscribeGroupDO;
import com.utils.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupService {

    @Autowired
    MongoDao mongoDao;

    @Autowired
    RedisDao redisDao;

    final static String userId = "admin";

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
        mongoDao.deleteGroup(groupId);

    }


    public void addTaskToGroup(String groupId, String taskId) {
        SubscribeGroupDO groupById = mongoDao.findGroupById(groupId);
        groupById.getTasks().add(taskId);
        mongoDao.updateGroup(groupById);

        //增加redis中的信息
        String subscribeKey = RedisConstant.getSubscribeKey(taskId);
        List<String> keywords = groupById.getKeywords();
        String s = GsonUtil.toJson(keywords);
        redisDao.addMap(subscribeKey, groupId, s);
    }


    public void removeTaskFromGroup(String groupId, String taskId) {
        SubscribeGroupDO groupById = mongoDao.findGroupById(groupId);
        groupById.getTasks().remove(taskId);
        mongoDao.updateGroup(groupById);

        //删除redis中的信息
        String subscribeKey = RedisConstant.getSubscribeKey(taskId);
        redisDao.delMap(subscribeKey, groupId);
    }

    public void changeActive(String groupId) {
        SubscribeGroupDO groupById = mongoDao.findGroupById(groupId);
        Boolean active = groupById.getActive();
        groupById.setActive(!active);
        mongoDao.updateGroup(groupById);
    }
}
