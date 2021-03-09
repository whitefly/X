package center.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class TaskUtil {
    static Pattern URL_PAT = Pattern.compile("^(http(s)?:\\/\\/)?([\\w-]+\\.)+[\\w-]+(:\\d{2,5})?(\\/?\\#?[\\w-.\\/?%&=]*)?$");


    //上线后这里要替换,才能加载资源
    static String baseUri = "http://localhost:8081";

    static List<String> LINK_ATTRS_TO_CHANGE = Arrays.asList("href", "src", "data-url", "data-src");

    //要给代理页面注入的js脚本
    final static List<String> CSS_HELPER_SCRIPT_TO_INSERT = Arrays.asList(
            "/lib/css-selector/lib/CssSelector.js",
            "/scripts/ContentSelector.js",
            "/lib/jquery-2.1.4.min.js",
            "/scripts/ContentSelector.js",
            "/scripts/ElementQuery.js",
            "/scripts/DataExtractor.js",
            "/lib/sugar-1.4.1.js",
            "/lib/jquery.whencallsequentially.js",
            "/lib/jquery.mloading.js",
            "/scripts/UniqueElementList.js",
            "/lib/ajaxhook.js"
    );

    final static List<String> CSS_HELPER_CSS_TO_INSERT = Arrays.asList(
            "/lib/bootstrap-3.3.7/css/bootstrap.css",
            "/css/content_script.css",
            "/css/jquery.mloading.css"
    );


    public static void change2AbsUrl(Document document) {
//        将可能的所有连接都进行替换
        //将相对连接,都变为绝对连接,处理标签为 a,link, script
        for (String attName : LINK_ATTRS_TO_CHANGE) {
            change2AbsUrl(document, attName);
        }
    }

    private static void change2AbsUrl(Document document, String attr) {
        Elements href = document.getElementsByAttribute(attr);
        for (Element e : href) {
            String abs = e.attr("abs:" + attr);
            e.attr(attr, abs);
        }
    }

    public static void insertCssHelperElement(Document document) {
        Elements meta = document.getElementsByTag("meta");
        if (!meta.isEmpty()) {
            Element element = meta.get(0);
            List<Element> scriptHelperElement = createScriptElement();
            element.insertChildren(0, scriptHelperElement);
        }
    }

    private static List<Element> createScriptElement() {
        List<Element> cssHelperElement = new ArrayList<>();

        //生成script链接标签
        for (String scriptLink : CSS_HELPER_SCRIPT_TO_INSERT) {
            Element element = new Element("script");
            element.attr("src", baseUri + scriptLink);
            cssHelperElement.add(element);
        }
        //生成css链接标签
        for (String cssLink : CSS_HELPER_CSS_TO_INSERT) {
            Element element = new Element("link");
            element.attr("href", baseUri + cssLink);
            element.attr("rel", "stylesheet");
            cssHelperElement.add(element);
        }
        return cssHelperElement;
    }

    public static void main(String[] args) throws MalformedURLException {

    }
}
