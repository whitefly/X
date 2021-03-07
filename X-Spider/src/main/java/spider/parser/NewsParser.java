package spider.parser;

import com.entity.ArticleDO;
import com.entity.FieldDO;
import com.entity.NewsParserDO;
import com.entity.TaskDO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import spider.utils.AutoExtractor;
import spider.utils.ReUtil;
import spider.utils.TimeUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.utils.FieldUtil.isFieldEmpty;

/**
 * 最底层的正文处理器,其他处理器最终都会解析正文,所以都会继承它
 */
@Slf4j
public class NewsParser implements PageProcessor {

    Site site = Site
            .me()
            .setSleepTime(0)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");


    @Getter
    TaskDO taskInfo;

    @Getter
    NewsParserDO newsParserDO;

    public NewsParser(TaskDO taskDO, NewsParserDO newsParserDO) {
        this.taskInfo = taskDO;
        this.newsParserDO = newsParserDO;
    }

    @Override
    public void process(Page page) {
        ArticleDO articleDO = NewsParser.parseArticle(page, newsParserDO);
        //把整个对象放入map中的ArticleDO中,在pipeline去存出
        page.putField("ArticleDO", articleDO);
    }

    public static ArticleDO parseArticle(Page page, NewsParserDO parserDetail) {
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

        //解析额外字段
        articleDO.setExtra(parseExtra(page, parserDetail));
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

    public static Map<String, Object> parseExtra(Page page, NewsParserDO parserDetail) {
        Map<String, Object> item = new HashMap<>();
        if (parserDetail.getExtra() == null) return item;
        for (FieldDO f : parserDetail.getExtra()) {
            item.put(f.getName(), getValue(page, f));
        }
        return item;
    }


    @Override
    public Site getSite() {
        return site;
    }
}
