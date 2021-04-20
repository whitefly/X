package spider.utils;

import com.entity.ArticleDO;
import com.entity.ExtraField;
import com.entity.FieldDO;
import com.entity.NewsParserDO;
import com.mytype.ExtraType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.*;
import java.util.stream.Collectors;

import static com.utils.FieldUtil.hasNoLocator;

@Slf4j
public class NewsParserUtil {


    public static ArticleDO parseArticle(Page page, NewsParserDO parserDetail) {
        FieldDO titleRule = parserDetail.getTitleRule();
        FieldDO contentRule = parserDetail.getContentRule();
        FieldDO timeRule = parserDetail.getTimeRule();

        //正文密度算法解析
        ArticleDO articleDO = new ArticleDO();
        AutoExtractor.News news = autoParse(page);
        Element autoScope = null;
        if (news != null) {
            autoScope = news.getContentElement();
            articleDO.setTitle(news.getTitle());
            articleDO.setContent(new Html(autoScope.text()).xpath("tidyText()").get());
            articleDO.setPtime(news.getDate());
            autoScope.setBaseUri(page.getRequest().getUrl());
        }

        String v;
        //填充正文(用户定位不成功,就采用自动算法)
        if (!hasNoLocator(contentRule)) {
            if ((v = getValue(page, contentRule)) != null) {
                articleDO.setContent(v);
            }
        }

        //填充标题
        if (!hasNoLocator(titleRule)) {
            if ((v = getValue(page, titleRule)) != null) {
                articleDO.setTitle(v);
            }
        }
        //填充时间
        if (!hasNoLocator(timeRule)) {
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
        Map<String, Object> extraRst = parseExtras(page, parserDetail, autoScope);
        articleDO.setExtra(extraRst);
        return articleDO;
    }

    public static Map<String, Object> parseExtras(Page page, NewsParserDO parserDetail, Element autoScope) {
        Map<String, Object> item = new HashMap<>();
        if (parserDetail.getExtra() == null) return item;
        for (ExtraField f : parserDetail.getExtra()) {
            if (!StringUtils.isEmpty(f.getName())) {
                item.put(f.getName(), parseExtra(page, f, autoScope));
            }
        }
        return item;
    }

    public static AutoExtractor.News autoParse(Page page) {
        Document document = page.getHtml().getDocument();
        AutoExtractor.News news = null;
        try {
            news = AutoExtractor.getNewsByDoc(document);
            //转换字符串时间为date格式
            Date date = TimeUtil.convert2Date(news.getTime());
            //若无法解析,则从整个html中匹配时间
            if (date == null) {
                date = TimeUtil.convert2Date(TimeUtil.extractTimeStr(page.getRawText()));
            }
            news.setDate(date);
        } catch (Exception e) {
            log.error("自动解析失败:" + page.getUrl());
        }
        return news;
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
            List<String> list = ReUtil.regex(f.getRe(), rawText, true, true);
            result = String.join(" ", list);
        }
        if (result != null) return result.trim();
        return null;
    }

    public static boolean notEmpty(String str) {
        return !StringUtils.isEmpty(str);
    }


    public static List<String> parseExtra(Page page, ExtraField f, Element autoScope) {
        //若用户填写了正则,则采取用户的正则提取
        if (!StringUtils.isEmpty(f.getRe())) {
            return ReUtil.regex(f.getRe(), page.getRawText(), true, true);
        }

        boolean targetForText = (ExtraType.text == f.getExtraType() || f.getExtraType() == null);
        if (targetForText) {
            return extractTextOrHtml(page, f, true);
        } else {
            //用户是否想缩小范围来提取(不缩小就采用自动算法获取的节点)
            boolean reduceScope = notEmpty(f.getCss()) || notEmpty(f.getXpath());

            //还是采用dom树来解析
            Elements scope = reduceScope ? getScopeElements(page.getHtml().getDocument(), f) : new Elements(autoScope != null ? autoScope : page.getHtml().getDocument());
            return extractLink(f, scope);
        }
    }

    private static List<String> extractLink(ExtraField f, Elements elements) {
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

    private static List<String> extractHtml(Elements elements) {
        return elements.stream().map(Element::html).collect(Collectors.toList());
    }

    private static List<String> extractTextOrHtml(Page page, FieldDO f, boolean text) {
        List<String> all = null;
        //选中符合的元素
        if (notEmpty(f.getCss())) {
            Selectable cssRst = page.getHtml().css(f.getCss());
            all = text ? cssRst.xpath("tidyText()").all() : cssRst.xpath("outerHtml()").all();
        } else if (notEmpty(f.getXpath())) {
            Selectable xpathRst = page.getHtml().xpath(f.getXpath());
            all = text ? xpathRst.xpath("tidyText()").all() : xpathRst.xpath("outerHtml()").all();
        }
        //trim
        if (CollectionUtils.isNotEmpty(all)) all = all.stream().map(String::trim).collect(Collectors.toList());
        return all;
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

    private static List<String> extractImgLink(Elements elements) {
        List<Element> result = new ArrayList<>();
        for (Element element : elements) {
            //Elements的attr只是找第一个含有这个attr的,然后返回
            Elements img = element.getElementsByTag("img");
            result.addAll(img);
        }
        List<String> imgLinks = result.stream().map(img -> {
            String imgUrl;
            if (StringUtils.isNotEmpty(imgUrl = img.attr("data-original"))) {
                //懒加载图片的地址
                return imgUrl;
            } else {
                return img.attr("abs:src");
            }
        }).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        return CollectionUtils.isNotEmpty(imgLinks) ? imgLinks : null;
    }

    private static List<String> extractVideoLink(Elements elements) {
        List<String> videoLinks = new ArrayList<>();
        for (Element element : elements) {
            String s = element.outerHtml();
            List<String> regex = ReUtil.regex(ReUtil.VIDEO_LINK_REGEX, s, true, false);
            if (CollectionUtils.isNotEmpty(regex)) regex.forEach(x -> videoLinks.add(x.replace("\\/", "/")));
        }
        return videoLinks;
    }

    private static List<String> extractFileLink(Elements elements) {
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
        return CollectionUtils.isNotEmpty(fileLinks) ? fileLinks : null;
    }
}
