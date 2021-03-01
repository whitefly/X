package com.web.controller;

import com.entity.ArticleDO;
import com.entity.ResponseVO;
import com.exception.WebException;
import com.google.gson.Gson;
import com.web.service.DocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

import static com.exception.ErrorCode.SERVICE_DOC_MISS_TASK_ID;
import static com.utils.DocUtil.convertToExcel;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/api/doc")
public class DocController {
    Gson gson = new Gson();

    @Autowired
    DocService docService;

    @PostMapping(path = "/list")
    public ResponseVO docList(@RequestBody String params) {
        HashMap hashMap = gson.fromJson(params, HashMap.class);
        int pageIndex = ((Double) hashMap.get("pageIndex")).intValue();
        String taskId = (String) hashMap.get("taskId");

        List<ArticleDO> articleDOS = docService.docList(taskId, pageIndex);
        return new ResponseVO(articleDOS);
    }

    @PostMapping(path = "/clear")
    public ResponseVO clearArticle(@RequestBody String params) {
        //清理每个任务的全部爬取结果
        HashMap hashMap = gson.fromJson(params, HashMap.class);
        String taskId = (String) hashMap.get("taskId");
        docService.clearDoc(taskId);
        return new ResponseVO();
    }

    @GetMapping(path = "/download")
    public void downloadExcel(HttpServletResponse response, @RequestParam(value = "pageIndex") Integer pageIndex,
                              @RequestParam(value = "taskId") String taskId) {
        //由于每个任务字段不同,此时要求必须传入taskId;
        log.info(taskId + " " + pageIndex);
        if (StringUtils.isEmpty(taskId)) throw new WebException(SERVICE_DOC_MISS_TASK_ID);
        List<ArticleDO> articleDOS = docService.docList(taskId, pageIndex);
        //将ArticleDO转为excel
        convertToExcel(response, articleDOS);
    }
}
