package center.web.service;


import com.dao.MongoDao;
import com.entity.SubscribeGroupDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupService {

    @Autowired
    MongoDao mongoDao;
    final static String userId = "admin";

    public void createGroup(String name) {
        SubscribeGroupDO subscribeGroupDO = new SubscribeGroupDO();
        subscribeGroupDO.setUserId(userId);
        mongoDao.insertGroup(subscribeGroupDO);
    }

    public void deleteGroup(String groupId) {
        mongoDao.deleteGroup(groupId);
    }


    public void addTaskToGroup(String groupId, String taskId) {
        SubscribeGroupDO groupById = mongoDao.findGroupById(groupId);
        groupById.getTasks().add(taskId);
        mongoDao.updateGroup(groupById);
    }


    public void removeTaskFromGroup(String groupId, String taskId) {
        SubscribeGroupDO groupById = mongoDao.findGroupById(groupId);
        groupById.getTasks().remove(taskId);
        mongoDao.updateGroup(groupById);

    }

    public void changeActive(String groupId) {
        SubscribeGroupDO groupById = mongoDao.findGroupById(groupId);
        Boolean active = groupById.getActive();
        groupById.setActive(!active);
        mongoDao.updateGroup(groupById);
    }


}
