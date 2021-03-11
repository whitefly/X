package spider.utils;

import com.entity.FieldDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用于提取Page页中的新链接+对新链接分类
 */
@Slf4j
public class RequestUtil {
    public static final String ALIAS_KEY = "alias";

    public static List<String> getLinksByField(Page page, FieldDO rule) {
        //field为提取连接的xpath
        List<String> indexUrls = null;

        if (!StringUtils.isEmpty(rule.getCss())) {
            indexUrls = page.getHtml().css(rule.getCss()).links().all();
        } else if (!StringUtils.isEmpty(rule.getXpath())) {
            indexUrls = page.getHtml().xpath(rule.getXpath()).links().all();
        } else if (!StringUtils.isEmpty(rule.getRe())) {
            indexUrls = page.getHtml().regex(rule.getRe()).all();
        }
        return indexUrls == null ? Collections.emptyList() : indexUrls;
    }

    public static List<String> getLinksByAjax(Page page, FieldDO rule) {
        //field为提取连接的xpath
        List<String> indexUrls = null;
        if (!StringUtils.isEmpty(rule.getXpath())) {
            indexUrls = page.getJson().jsonPath(rule.getXpath()).all();
        }
        return indexUrls == null ? Collections.emptyList() : indexUrls;
    }

    public static List<Request> extractAliasRequest(Page page, FieldDO rule, String alias) {
        List<String> linksByField = getLinksByField(page, rule);
        return convert2AliasRequest(linksByField, alias);
    }

    public static void addToQueue(Page page, List<Request> requests) {
        if (!CollectionUtils.isEmpty(requests)) {
            requests.forEach(page::addTargetRequest);
        }
    }

    /**
     * 给Request设置深度,方便调用不同的解析函数.大致模仿回调的感觉
     *
     * @param urls
     * @return
     */
    public static List<Request> convert2AliasRequest(List<String> urls, String alias) {
        List<Request> rnt = new ArrayList<>();
        for (String url : urls) {
            if (StringUtils.isBlank(url) || url.equals("#") || url.startsWith("javascript:")) continue;

            Request request = new Request();
            request.setUrl(url);
            request.putExtra(ALIAS_KEY, alias);
            rnt.add(request);
        }
        return rnt;
    }

    public static Object getAlias(Request request) {
        return request.getExtra(ALIAS_KEY);
    }
}
