package center.web.controller;

import center.exception.ErrorCode;
import center.exception.WebException;
import center.web.service.TaskService;
import com.entity.*;
import com.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static center.exception.ErrorCode.SERVICE_TASK_NOT_EXIST;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    TaskService taskService;


    @PostMapping(path = "/create")
    public ResponseVO createTask(@RequestBody String params) {
        TaskEditVO taskEditVO = GsonUtil.fromJson(params, TaskEditVO.class);
        TaskDO task = taskEditVO.getTask();
        NewsParserDO parser = taskEditVO.getParser();
        taskService.addTask(task, parser);
        return new ResponseVO();
    }

    /**
     * 切换任务状态
     *
     * @return
     */
    @PostMapping(path = "/switch")
    public ResponseVO stopTask(@RequestBody String param) {
        Map map = GsonUtil.fromJson(param, Map.class);
        String taskId = (String) map.get("taskId");
        boolean state = (boolean) map.get("state");
        log.info("expect state[{}]: taskId:{}", state, taskId);
        if (state) taskService.startTask(taskId);
        else taskService.stopTask(taskId);

        return new ResponseVO();
    }


    /**
     * 更新任务的配置
     *
     * @return
     */
    @PostMapping(path = "/update")
    public ResponseVO updateTask(@RequestBody String params) {
        TaskEditVO taskEditVO = GsonUtil.fromJson(params, TaskEditVO.class);
        TaskDO task = taskEditVO.getTask();
        NewsParserDO parser = taskEditVO.getParser();


        taskService.updateTask(task, parser);
        return new ResponseVO();
    }

    /**
     * 删除任务
     *
     * @return
     */
    @PostMapping(path = "/del")
    public ResponseVO delTask(@RequestBody String params) {
        Map map = GsonUtil.fromJson(params, Map.class);
        String taskId = (String) map.get("taskId");

        taskService.removeTask(taskId);
        return new ResponseVO();
    }

    @PostMapping(path = "/tempStart")
    public ResponseVO tempStart(@RequestBody String params) {
        Map map = GsonUtil.fromJson(params, Map.class);
        String taskId = (String) map.get("taskId");
        taskService.temporaryStart(taskId);
        return new ResponseVO();
    }

    @GetMapping(path = "/list")
    public ResponseVO taskList1(@RequestParam(value = "parseType", required = false) String parseType, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "pageIndex") Integer pageIndex, @RequestParam(value = "pageSize") Integer pageSize) {
        List<TaskDO> tasks = taskService.getTasks(pageIndex, pageSize, name, parseType);
        long taskCount = taskService.getTaskCount(name, parseType);
        PageVO<TaskDO> taskDOPageVO = new PageVO<>(taskCount, tasks);
        return new ResponseVO(taskDOPageVO);
    }

    @PostMapping(path = "/query")
    public ResponseVO queryTask(@RequestBody String params) {
        Map map = GsonUtil.fromJson(params, Map.class);
        String taskId = (String) map.get("taskId");
        TaskDO task = taskService.findTask(taskId);
        if (task == null) throw new WebException(SERVICE_TASK_NOT_EXIST);
        NewsParserDO indexParser = taskService.findNewsParser(task.getParserId());
        TaskEditVO taskEditVO = new TaskEditVO();
        taskEditVO.setTask(task);
        taskEditVO.setParser(indexParser);
        return new ResponseVO(taskEditVO);
    }

    //所有模板测试都走这个统一的接口
    @PostMapping(path = "/test/template")
    public ResponseVO testTemplate(@RequestBody String params) {
        TaskEditVO taskEditVO = GsonUtil.fromJson(params, TaskEditVO.class);
        TaskDO task = taskEditVO.getTask();
        NewsParserDO parser = taskEditVO.getParser();
        TestInfo testInfo = taskService.testParser(task, parser);
        if (testInfo != null) {
            return new ResponseVO(testInfo);
        } else {
            return new ResponseVO(-1, "测试结果为空");
        }
    }


    @PostMapping(path = "/test/bodyParser")
    public ResponseVO testBody(@RequestBody String params) {
        TaskEditVO taskEditVO = GsonUtil.fromJson(params, TaskEditVO.class);
        TaskDO task = taskEditVO.getTask();
        NewsParserDO parser = taskEditVO.getParser();

        if (parser != null) {
            String url = taskEditVO.getTargetUrl();
            if (parser.getBlockSplit() == null || !parser.getBlockSplit()) {
                Map<String, Object> rnt = taskService.testBody(task, parser, url);
                return new ResponseVO(rnt);
            } else {
                List<Map<String, Object>> rnt = taskService.testBodyByBlock(task, parser, url);
                return new ResponseVO(rnt);
            }
        } else {
            return new ResponseVO(ErrorCode.SERVICE_ERROR);
        }
    }
}