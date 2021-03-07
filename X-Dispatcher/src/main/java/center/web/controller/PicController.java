package center.web.controller;

import center.web.service.DocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

@CrossOrigin
@Slf4j
@RequestMapping(value = "/api/picture")
@RestController
public class PicController {

    @Autowired
    DocService docService;


    @GetMapping(value = "/showPicture")
    public String show(@RequestParam(name = "articleId") String articleId) {
        List<String> imgTags = docService.getPicUrlByArticleId(articleId);
        //转为image标签
        String picHtml = String.join(" ", imgTags);

        String html = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Title</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" + picHtml +
                "</body>\n" +
                "</html>";
        return html;
    }
}
