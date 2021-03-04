package center.web.controller;

import com.entity.IndexParserDO;
import com.entity.ResponseVO;
import com.entity.TaskDO;
import com.entity.TaskEditVO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.utils.ParserUtil.typeAdapter;

@CrossOrigin
@Controller
@Slf4j
public class HelloController {


    Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeAdapter).create();


    @ResponseBody
    @GetMapping(path = "api/hello")
    public ResponseVO hello() {
        return new ResponseVO();
    }

    @ResponseBody
    @PostMapping(path = "api/post")
    public ResponseVO post(@RequestBody String data) {
        System.out.println(data);
        TaskEditVO taskEditVO = gson.fromJson(data, TaskEditVO.class);
        TaskDO task = taskEditVO.getTask();
        IndexParserDO parser = taskEditVO.getParser();
        System.out.println(parser.getIndexRule());
        System.out.println(parser);
        return new ResponseVO();
    }
}
