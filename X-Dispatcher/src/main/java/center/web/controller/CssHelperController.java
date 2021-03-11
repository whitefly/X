package center.web.controller;

import center.utils.DynamicUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.io.IOException;

import center.utils.CssHelperUtil;

@CrossOrigin
@Controller
@Slf4j
public class CssHelperController {


    @GetMapping(path = "api/help")
    public String helper() {
        return "/helper.html";
    }

    @PostMapping(path = "api/proxy")
    @ResponseBody
    public String proxy(@RequestParam(value = "url") String url, @RequestParam(value = "selenium", defaultValue = "false") boolean selenium) {
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            url = "http://" + url;
        }

        try {
            String htmlStr = null;
            if (selenium) {
                //动态加载
                Request request = new Request(url);
                Page download = DynamicUtil.dynamicDownloader.download(request, null);
                htmlStr = download.getRawText();
            } else {
                //直接采用jsoup来访问
                Connection connect = Jsoup.connect(url);
                Document document = connect.get();
                htmlStr = document.outerHtml();
            }

            Document document = Jsoup.parse(htmlStr, url);
            //若网页调用域内的css和js,会导致资源无法加载出来.此时用正则统一替换为绝对引用base
            CssHelperUtil.change2AbsUrl(document);
            //注入js和css脚本
            CssHelperUtil.insertCssHelperElement(document);

            return document.outerHtml();
        } catch (IOException e) {
            log.warn("网页下载失败", e);
            return "error";
        }
    }
}
