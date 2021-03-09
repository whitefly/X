package spider.utils;

import com.constant.ExtraType;
import com.entity.ArticleDO;
import com.entity.FieldDO;
import com.entity.NewsParserDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.*;
import java.util.stream.Collectors;

import static com.utils.FieldUtil.isFieldEmpty;

@Slf4j
public class NewsParserUtil {


    public static ArticleDO parseArticle(Page page, NewsParserDO parserDetail) {
        FieldDO titleRule = parserDetail.getTitleRule();
        FieldDO contentRule = parserDetail.getContentRule();
        FieldDO timeRule = parserDetail.getTimeRule();

        //正文密度算法解析
        ArticleDO articleDO = autoParse(page);


        String v;
        //填充正文(用户定位不成功,就采用自动算法)
        if (!isFieldEmpty(contentRule)) {
            if ((v = getValue(page, contentRule)) != null) {
                articleDO.setContent(v);
            }
        }

        //填充标题
        if (!isFieldEmpty(titleRule)) {
            if ((v = getValue(page, titleRule)) != null) {
                articleDO.setTitle(v);
            }
        }
        //填充时间
        if (!isFieldEmpty(timeRule)) {
            String value = getValue(page, timeRule);
            String s = TimeUtil.extractTimeStr(value);
            Date date = TimeUtil.convert2Date(s);
            if (date != null) articleDO.setPtime(date);
        }

        //若时间为空,则用现在的时间替代
        if (articleDO.getPtime() == null) {
            articleDO.setPtime(new Date());
        }
        articleDO.setCtime(new Date());

        //解析额外字段
        Map<String, Object> extraRst = parseExtras(page, parserDetail);
        articleDO.setExtra(extraRst);
        return articleDO;
    }

    public static Map<String, Object> parseExtras(Page page, NewsParserDO parserDetail) {
        Map<String, Object> item = new HashMap<>();
        if (parserDetail.getExtra() == null) return item;
        for (FieldDO f : parserDetail.getExtra()) {
            if (!StringUtils.isEmpty(f.getName())) {
                item.put(f.getName(), parseExtra(page, f));
            }
        }
        return item;
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
        } catch (Exception e) {
            log.error("自动解析失败:" + page.getUrl());
        }
        return result;
    }

    public static String getValue(Page page, FieldDO f) {
        String result = null;
        if (!StringUtils.isEmpty(f.getCss())) {
            List<String> all = page.getHtml().css(f.getCss()).xpath("allText()").all();
            result = all != null ? String.join("\n", all) : null;
        } else if (!StringUtils.isEmpty(f.getXpath())) {
            List<String> all = page.getHtml().xpath(f.getXpath()).xpath("allText()").all();
            result = all != null ? String.join("\n", all) : null;
        } else if (!StringUtils.isEmpty(f.getRe())) {
            String rawText = page.getRawText();
            result = ReUtil.regex(f.getRe(), rawText, true, true);
        } else if (f.getSpecial() != null) {
            result = f.getSpecial();
        }
        if (result != null) return result.trim();
        return null;
    }

    public static boolean notEmpty(String str) {
        return !StringUtils.isEmpty(str);
    }


    public static String parseExtra(Page page, FieldDO f) {
        //若用户填写了正则,则采取用户的正则提取
        if (!StringUtils.isEmpty(f.getRe())) {
            return ReUtil.regex(f.getRe(), page.getRawText(), true, true);
        }

        boolean targetForText = (ExtraType.text == f.getExtraType() || f.getExtraType() == null);
        if (targetForText) {
            return extractTextOrHtml(page, f, true);
        } else {
            //用户是否想缩小范围来提取
            boolean reduceScope = notEmpty(f.getCss()) || notEmpty(f.getXpath());

            //还是采用dom树来解析
            Elements scope = reduceScope ? getScopeElements(page.getHtml().getDocument(), f) : new Elements(page.getHtml().getDocument());
            return extractLink(f, scope);
        }
    }

    private static String extractLink(FieldDO f, Elements elements) {
        if (elements == null) return null;
        //开始提取链接
        switch (f.getExtraType()) {
            case html:
                return extractHtml(elements);
            case picture:
                return extractImgLink(elements);
            case video:
                return extractVideoLink(elements);
            case file:
                return extractFileLink(elements);
            default:
                return null;
        }
    }

    private static String extractHtml(Elements elements) {
        List<String> htmlContent = elements.stream().map(Element::html).collect(Collectors.toList());
        return String.join(" \n ", htmlContent);
    }

    private static String extractTextOrHtml(Page page, FieldDO f, boolean text) {
        List<String> all = null;
        //选中符合的元素
        if (notEmpty(f.getCss())) {
            Selectable cssRst = page.getHtml().css(f.getCss());
            all = text ? cssRst.xpath("tidyText()").all() : cssRst.xpath("outerHtml()").all();
        } else if (notEmpty(f.getXpath())) {
            Selectable xpathRst = page.getHtml().xpath(f.getXpath());
            all = text ? xpathRst.xpath("tidyText()").all() : xpathRst.xpath("outerHtml()").all();
        }
        //不同元素的内容合并,text采用\n作为分隔,html作为空格进行分隔
        String delimiter = text ? "\n" : " ";
        return all != null ? String.join(delimiter, all) : null;
    }

    private static Elements getScopeElements(Document doc, FieldDO f) {
        // TODO: 2021/3/9 解决只能用css来缩小元素范围的问题
        //由于框架支持度有限,只能通过css来缩小范围,xpath传入无效,还是返回整个dom树
        if (notEmpty(f.getCss())) {
            String css = f.getCss();
            return doc.select(css);
        } else {
            return new Elements(doc);
        }
    }

    private static String extractImgLink(Elements elements) {
        List<Element> result = new ArrayList<>();
        for (Element element : elements) {
            //Elements的attr只是找第一个含有这个attr的,然后返回
            Elements img = element.getElementsByTag("img");
            result.addAll(img);
        }
        List<String> imgLinks = result.stream().map(img -> img.attr("abs:src")).collect(Collectors.toList());
        return CollectionUtils.isNotEmpty(imgLinks) ? String.join(" \n ", imgLinks) : null;
    }

    private static String extractVideoLink(Elements elements) {
        List<String> videoLinks = new ArrayList<>();
        for (Element element : elements) {
            String s = element.outerHtml();
            String regex = ReUtil.regex(ReUtil.VIDEO_LINK_REGEX, s, true, false);
            if (regex != null) videoLinks.add(regex.replace("\\/", "/"));
        }
        return String.join(" \n ", videoLinks);
    }

    private static String extractFileLink(Elements elements) {
        List<String> fileSuffix = Arrays.asList(".docx", ".doc", ".pdf");
        List<Element> result = new ArrayList<>();
        for (Element element : elements) {
            Elements links = element.getElementsByTag("a");
            //过滤后缀为文件类型的a标签
            List<Element> fileHref = links.stream().filter(e -> {
                String href = e.attr("href");
                return fileSuffix.stream().anyMatch(href::endsWith);
            }).collect(Collectors.toList());
            result.addAll(fileHref);
        }
        //转为绝对链接
        List<String> fileLinks = result.stream().map(fileLink -> fileLink.attr("abs:href")).collect(Collectors.toList());
        return CollectionUtils.isNotEmpty(fileLinks) ? String.join(" \n ", fileLinks) : null;
    }
}
