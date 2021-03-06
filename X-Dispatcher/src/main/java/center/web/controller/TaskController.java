package center.web.controller;

import com.entity.*;
import center.exception.WebException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import center.web.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static center.exception.ErrorCode.SERVICE_TASK_NOT_EXIST;
import static com.utils.ParserUtil.typeAdapter;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    TaskService taskService;


    Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeAdapter).create();


    @PostMapping(path = "/create")
    public ResponseVO createTask(@RequestBody String params) {
        TaskEditVO taskEditVO = gson.fromJson(params, TaskEditVO.class);
        TaskDO task = taskEditVO.getTask();
        IndexParserDO parser = taskEditVO.getParser();
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
        Map map = gson.fromJson(param, Map.class);
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
        TaskEditVO taskEditVO = gson.fromJson(params, TaskEditVO.class);
        TaskDO task = taskEditVO.getTask();
        IndexParserDO parser = taskEditVO.getParser();


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
        Map map = gson.fromJson(params, Map.class);
        String taskId = (String) map.get("taskId");

        taskService.removeTask(taskId);
        return new ResponseVO();
    }

    @GetMapping(path = "/list")
    public ResponseVO taskList1(@RequestParam(value = "name") String name, @RequestParam(value = "pageIndex") Integer pageIndex, @RequestParam(value = "pageSize") Integer pageSize) {
        List<TaskDO> tasks = taskService.getTasks(pageIndex, pageSize, name);
        long taskCount = taskService.getTaskCount();
        PageVO<TaskDO> taskDOPageVO = new PageVO<>(taskCount, tasks);
        return new ResponseVO(taskDOPageVO);
    }

    @PostMapping(path = "/query")
    public ResponseVO queryTask(@RequestBody String params) {
        Map map = gson.fromJson(params, Map.class);
        String taskId = (String) map.get("taskId");
        TaskDO task = taskService.findTask(taskId);
        if (task == null) throw new WebException(SERVICE_TASK_NOT_EXIST);
        IndexParserDO indexParser = taskService.findIndexParser(task.getParserId());
        TaskEditVO taskEditVO = new TaskEditVO();
        taskEditVO.setTask(task);
        taskEditVO.setParser(indexParser);
        return new ResponseVO(taskEditVO);
    }


    /**
     * 目录解析测试,testUrl是目录url
     *
     * @return
     */
    @PostMapping(path = "/test/indexParse")
    public ResponseVO testIndex(@RequestBody String params) {
        TaskEditVO taskEditVO = gson.fromJson(params, TaskEditVO.class);
        TaskDO task = taskEditVO.getTask();
        IndexParserDO parser = taskEditVO.getParser();

        List<String> links = taskService.testIndex(task, parser);
        return new ResponseVO(links);
    }

    /**
     * 正文解析,testUrl是二级目录
     *
     * @return
     */
    @PostMapping(path = "/test/bodyParse")
    public ResponseVO testBody(@RequestBody String params) {
        TaskEditVO taskEditVO = gson.fromJson(params, TaskEditVO.class);
        TaskDO task = taskEditVO.getTask();
        IndexParserDO parser = taskEditVO.getParser();
        String url = taskEditVO.getTargetUrl();
        Map<String, Object> rnt = taskService.testBody(task, parser, url);
        return new ResponseVO(rnt);
    }
}
