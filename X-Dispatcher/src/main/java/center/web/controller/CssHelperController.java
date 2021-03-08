package center.web.controller;

import center.utils.DynamicUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static center.utils.TaskUtil.addCssHelper;
import static center.utils.TaskUtil.change2AbsUrl;

@CrossOrigin
@Controller
@Slf4j
public class CssHelperController {

    RestTemplate restTemplate = new RestTemplate();

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
            String result = null;
            URL u = new URL(url);
            if (selenium) {
                //动态加载
                Request request = new Request(url);
                Page download = DynamicUtil.dynamicDownloader.download(request, null);
                result = download.getRawText();
            } else {
                //静态加载
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.182 Safari/537.36");
                headers.set("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-TW;q=0.6");
                headers.set("Accept-Encoding", "application/gzip");
                HttpEntity entity = new HttpEntity(headers);
                ResponseEntity<byte[]> exchange = restTemplate.exchange(u.toExternalForm(), HttpMethod.GET, entity, byte[].class);
                byte[] body = exchange.getBody();
                if (body != null) result = new String(body, StandardCharsets.UTF_8);
            }


            if (result != null) {
                //若网页调用域内的css和js,会导致资源无法加载出来.此时用正则统一替换为绝对引用base
                result = change2AbsUrl(result, u);
                result = addCssHelper(result);
            }

            //在html中注入js的辅助选择模块
            return result;
        } catch (MalformedURLException e) {
            return "";
        }
    }
}
