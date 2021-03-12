package center.web.controller;

import center.web.service.GroupService;
import com.entity.ResponseVO;
import com.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/api/group")
public class GroupController {

    @Autowired
    GroupService groupService;

    @PostMapping("/create")
    public ResponseVO createGroup(@RequestBody String params) {
        Map map = GsonUtil.fromJson(params, Map.class);
        String groupName = (String) map.get("groupName");
        groupService.createGroup(groupName);
        return new ResponseVO();
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
        Map map = GsonUtil.fromJson(params, Map.class);
        String groupId = (String) map.get("groupId");
        String taskId = (String) map.get("taskId");
        groupService.addTaskToGroup(groupId, taskId);
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

}
