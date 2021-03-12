package center.web.controller;

import com.entity.*;
import com.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Controller
@Slf4j
public class HelloController {


    @ResponseBody
    @GetMapping(path = "api/hello")
    public ResponseVO hello() {
        return new ResponseVO();
    }

    @ResponseBody
    @PostMapping(path = "api/post")
    public ResponseVO post(@RequestBody String data) {
        System.out.println(data);
        TaskEditVO taskEditVO = GsonUtil.fromJson(data, TaskEditVO.class);
        TaskDO task = taskEditVO.getTask();
        NewsParserDO parser = taskEditVO.getParser();
        System.out.println(parser.toString());
        return new ResponseVO();
    }
}
