package center.web.controller;

import com.entity.ArticleDO;
import com.entity.PageVO;
import com.entity.ResponseVO;
import center.exception.WebException;
import com.google.gson.Gson;
import center.web.service.DocService;
import com.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

import static center.exception.ErrorCode.SERVICE_DOC_MISS_TASK_ID;
import static center.utils.DocUtil.convertToExcel;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/api/doc")
public class DocController {

    @Autowired
    DocService docService;

    @PostMapping(path = "/list")
    public ResponseVO docList(@RequestBody String params) {
        HashMap hashMap = GsonUtil.fromJson(params, HashMap.class);
        int pageIndex = ((Double) hashMap.get("pageIndex")).intValue();
        String taskId = (String) hashMap.get("taskId");
        String keyword = (String) hashMap.get("keyword");
        int pageSize = ((Double) hashMap.get("pageSize")).intValue();


        if (StringUtils.isEmpty(taskId)) taskId = null;

        List<ArticleDO> docByTaskId = docService.getDocByTaskId(taskId, keyword, pageIndex, pageSize);

        long count = docService.getDocCountByTaskId(taskId, keyword);
        PageVO<ArticleDO> articleDOPageVO = new PageVO<>(count, docByTaskId);
        return new ResponseVO(articleDOPageVO);
    }

    @PostMapping(path = "/clear")
    public ResponseVO clearArticle(@RequestBody String params) {
        //清理每个任务的全部爬取结果
        HashMap hashMap = GsonUtil.fromJson(params, HashMap.class);
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
        List<ArticleDO> articleDOS = docService.getDocByTaskId(taskId);
        //将ArticleDO转为excel
        convertToExcel(response, articleDOS);
    }


}
