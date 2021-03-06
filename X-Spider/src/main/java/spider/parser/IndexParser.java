package spider.parser;

import com.constant.RedisConstant;
import com.dao.RedisDao;
import com.entity.*;
import lombok.Setter;
import spider.utils.AutoExtractor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import spider.utils.ReUtil;
import spider.utils.TimeUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.util.*;
import java.util.stream.Collectors;

import static com.utils.FieldUtil.isFieldEmpty;

@Slf4j
public class IndexParser implements PageProcessor {

    @Getter
    TaskDO taskInfo;

    @Getter
    IndexParserDO indexParser;


    @Setter
    RedisDao redisDao; //用来在访问redis前,扫一下是否之前访问过.若没有注入进来,就说明不需要


    private Site site = Site
            .me()
            .setSleepTime(0)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");


    public IndexParser(TaskDO taskInfo, IndexParserDO indexParser) {
        this.taskInfo = taskInfo;
        this.indexParser = indexParser;
    }

    @Override
    public void process(Page page) {
        if (taskInfo.getStartUrl().equals(page.getUrl().toString())) {
            //一级页面
            List<String> urls = parseIndexPage(page, indexParser);
            urls = filterNewsUrl(urls);
            if (!CollectionUtils.isEmpty(urls)) page.addTargetRequests(urls);
            page.setSkip(true);
        } else {
            //正文页面
            ArticleDO articleDO = parseArticle(page, indexParser);
            if (indexParser.getExtra() != null) {
                articleDO.setExtra(parseExtra(page, indexParser));
            }
            //把整个对象放入map中的ArticleDO中,在pipeline去存出
            page.putField("ArticleDO", articleDO);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public List<String> parseIndexPage(Page page, IndexParserDO parserDetail) {
        return getLinksByField(page, parserDetail.getIndexRule());
    }

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

    public static List<Request> getAliasLinksByField(Page page, FieldDO rule, String alias) {
        List<String> linksByField = getLinksByField(page, rule);
        return convert2AliasRequest(linksByField, alias);
    }

    public static ArticleDO parseArticle(Page page, NewsParserBO parserDetail) {
        FieldDO titleRule = parserDetail.getTitleRule();
        FieldDO contentRule = parserDetail.getContentRule();
        FieldDO timeRule = parserDetail.getTimeRule();
        ArticleDO articleDO = null;

        if (isFieldEmpty(titleRule) || isFieldEmpty(contentRule) || isFieldEmpty(timeRule)) {
            //若必要字段xpath缺失,则采用自动提取
            articleDO = autoParse(page);
        }

        if (articleDO == null) {
            articleDO = new ArticleDO();
        }

        //填充用户填写的字段
        if (!isFieldEmpty(contentRule)) articleDO.setContent(getValue(page, contentRule));
        if (!isFieldEmpty(titleRule)) articleDO.setTitle(getValue(page, titleRule));
        if (!isFieldEmpty(timeRule)) {
            String value = getValue(page, timeRule);
            //从用户选择的内容中提取时间
            String s = TimeUtil.extractTimeStr(value);
            Date date = TimeUtil.convert2Date(s);
            articleDO.setPtime(date);
        }

        //若时间为空,则用现在的时间替代
        if (articleDO.getPtime() == null) {
            articleDO.setPtime(new Date());
        }

        articleDO.setCtime(new Date());

        return articleDO;
    }

    public static ArticleDO autoParse(Page page) {
        Document document = page.getHtml().getDocument();
        ArticleDO result = new ArticleDO();
        try {
            AutoExtractor.News news = AutoExtractor.getNewsByDoc(document);
            result.setTitle(news.getTitle());

            String text = news.getContentElement().text();
            if (text != null) {
                text = new Html(news.getContentElement().text()).xpath("tidyText()").get();
            }
            result.setContent(text);
            //转换字符串时间为date格式
            Date date = TimeUtil.convert2Date(news.getTime());

            //若无法解析,则从整个html中匹配时间
            if (date == null) {
                date = TimeUtil.convert2Date(TimeUtil.extractTimeStr(page.getRawText()));
            }

            result.setPtime(date);
            return result;
        } catch (Exception e) {
            log.error("自动解析失败:" + page.getUrl(), e);
        }
        return null;
    }


    public static Map<String, Object> parseExtra(Page page, NewsParserBO parserDetail) {
        Map<String, Object> item = new HashMap<>();
        if (parserDetail.getExtra() == null) return item;
        for (FieldDO f : parserDetail.getExtra()) {
            item.put(f.getName(), getValue(page, f));
        }
        return item;
    }

    public static String getValue(Page page, FieldDO f) {
        String result = null;
        if (!StringUtils.isEmpty(f.getCss())) {
            result = page.getHtml().css(f.getCss()).xpath("allText()").get();
        } else if (!StringUtils.isEmpty(f.getXpath())) {
            result = page.getHtml().xpath(f.getXpath()).xpath("allText()").get();
        } else if (!StringUtils.isEmpty(f.getRe())) {
            String rawText = page.getRawText();
            result = ReUtil.regex(f.getRe(), rawText, true);
        } else if (f.getSpecial() != null) {
            result = f.getSpecial();
        }
        if (result != null) return result.trim();
        return null;
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
            request.putExtra("alias", alias);
            rnt.add(request);
        }
        return rnt;
    }

    public static Object getAlias(Request request) {
        return request.getExtra("alias");
    }

    public List<String> filterNewsUrl(List<String> newsUrl) {
        if (redisDao == null || CollectionUtils.isEmpty(newsUrl)) return newsUrl;

        //若这个url被当做正文页解析过,则忽略它
        String key = RedisConstant.getExtractedKey(taskInfo.getId());
        List<String> collect = newsUrl.stream().filter(url -> !redisDao.existInSet(key, url)).collect(Collectors.toList());
        int ignoreCount = newsUrl.size() - collect.size();
        log.info("{}:待访问url集合中发现过去已被提取正文的正文URL {} 个...跳过", taskInfo.getName(), ignoreCount);
        return collect;
    }

    public List<Request> filterNewsRequest(List<Request> newsRequest) {
        if (redisDao == null || CollectionUtils.isEmpty(newsRequest)) return newsRequest;
        //若这个url被当做正文页解析过,则忽略它
        String key = RedisConstant.getExtractedKey(taskInfo.getId());
        List<Request> collect = newsRequest.stream().filter(r -> !redisDao.existInSet(key, r.getUrl())).collect(Collectors.toList());
        int ignoreCount = newsRequest.size() - collect.size();
        log.info("{}:待访问url集合中发现过去已被提取正文的正文URL {} 个...跳过", taskInfo.getName(), ignoreCount);
        return collect;
    }
}
