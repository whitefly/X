package center.web.controller;

import center.web.service.GroupService;
import center.web.service.TaskService;
import com.entity.ResponseVO;
import com.entity.SubscribeGroupDO;
import com.entity.TaskDO;
import com.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/api/group")
public class GroupController {

    @Autowired
    GroupService groupService;

    @Autowired
    TaskService taskService;

    @PostMapping("/create")
    public ResponseVO createGroup(@RequestBody String params) {
        Map map = GsonUtil.fromJson(params, Map.class);
        String groupName = (String) map.get("groupName");
        groupService.createGroup(groupName);
        return new ResponseVO();
    }

    @PostMapping("/list")
    public ResponseVO groupList() {
        List<SubscribeGroupDO> subscribeGroupDOS = groupService.groupList();
        return new ResponseVO(subscribeGroupDOS);
    }

    @PostMapping("/del")
    public ResponseVO delGroup(@RequestBody String params) {
        Map map = GsonUtil.fromJson(params, Map.class);
        String groupId = (String) map.get("groupId");
        groupService.deleteGroup(groupId);
        return new ResponseVO();
    }


    @PostMapping("/addTask")
    public ResponseVO addTask(@RequestBody String params) {
        //每次单个增加任务改为批量增加任务
        Map map = GsonUtil.fromJson(params, Map.class);
        String groupId = (String) map.get("groupId");
        List<String> taskIds = (List<String>) map.get("taskIds");
        groupService.addTaskToGroup(groupId, taskIds);
        return new ResponseVO();
    }

    @PostMapping("/updateKeyword")
    public ResponseVO addKeyWord(@RequestBody String params) {
        Map map = GsonUtil.fromJson(params, Map.class);
        String groupId = (String) map.get("groupId");
        List<String> keyWords = (List<String>) map.get("keywords");
        groupService.updateKeywords(groupId, keyWords);
        return new ResponseVO();
    }


    @PostMapping("/removeTask")
    public ResponseVO removeTask(@RequestBody String params) {
        Map map = GsonUtil.fromJson(params, Map.class);
        String groupId = (String) map.get("groupId");
        String taskId = (String) map.get("taskId");
        groupService.removeTaskFromGroup(groupId, taskId);
        return new ResponseVO();
    }

    @PostMapping("/switch")
    public ResponseVO switchState(@RequestBody String params) {
        Map map = GsonUtil.fromJson(params, Map.class);
        String groupId = (String) map.get("groupId");
        groupService.changeActive(groupId);
        return new ResponseVO();
    }

    @PostMapping("/taskMapping")
    public ResponseVO taskMapping(@RequestBody String params) {
        Map map = GsonUtil.fromJson(params, Map.class);
        List<String> taskIds = (List<String>) map.get("taskIds");
        List<TaskDO> tasksByIds = taskService.findTasksByIds(taskIds);
        Map<String, String> nameMapping = new HashMap<>();
        tasksByIds.forEach(x -> nameMapping.put(x.getId(), x.getName()));
        return new ResponseVO(nameMapping);
    }

    @PostMapping("/allTaskMapping")
    public ResponseVO allTaskMapping() {
        List<TaskDO> tasksByIds = taskService.taskDOList();
        Map<String, String> nameMapping = new HashMap<>();
        tasksByIds.forEach(x -> nameMapping.put(x.getId(), x.getName()));
        return new ResponseVO(nameMapping);
    }
}
