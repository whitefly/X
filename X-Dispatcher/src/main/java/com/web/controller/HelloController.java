package com.web.controller;

import com.entity.IndexParserDO;
import com.entity.ResponseVO;
import com.entity.TaskDO;
import com.entity.TaskEditVO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.utils.ParserUtil.typeAdapter;
import static com.utils.TaskUtil.addCssHelper;
import static com.utils.TaskUtil.change2AbsUrl;

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
