package com.utils;

import com.entity.FieldDO;
import org.apache.http.client.utils.URLEncodedUtils;
import org.quartz.CronExpression;
import org.springframework.util.StringUtils;
import sun.net.util.URLUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskUtil {
    static Pattern URL_PAT = Pattern.compile("^(http(s)?:\\/\\/)?([\\w-]+\\.)+[\\w-]+(:\\d{2,5})?(\\/?\\#?[\\w-.\\/?%&=]*)?$");





    public static String change2AbsUrl(String html, URL u) {
        //将相对连接,都变为绝对连接,处理标签为 a,link, script

        //匹配根路径
        String src1 = "((?:href|src|data-url)=\")(/\\S+?)(\")";
        html = html.replaceAll(src1, "$1" + u + "$2$3");
        //相对路径
        String src2 = "((?:href|src|data-url)=\")(\\.\\.\\S+?)(\")";
        html = html.replaceAll(src2, "$1" + u.toExternalForm() + "$2$3");

        //将辅助选择库的js注入代理的html中
        return html;
    }

    public static String addCssHelper(String html) {
        //添加自动选择模块
        String js = "<script src=\"http://localhost:8081/lib/css-selector/lib/CssSelector.js\"></script>\n" +
                "<script src=\"http://localhost:8081/scripts/ContentSelector.js\"></script>\n" +
                "<script src=\"http://localhost:8081/lib/jquery-2.1.4.min.js\"></script>\n" +
                "<script src=\"http://localhost:8081/scripts/ContentSelector.js\"></script>\n" +
                "<script src=\"http://localhost:8081/scripts/ElementQuery.js\"></script>\n" +
                "<script src=\"http://localhost:8081/scripts/DataExtractor.js\"></script>\n" +
                "<script src=\"http://localhost:8081/lib/sugar-1.4.1.js\"></script>\n" +
                "<script src=\"http://localhost:8081/lib/jquery.whencallsequentially.js\"></script>\n" +
                "<script src=\"http://localhost:8081/lib/jquery.mloading.js\"></script>\n" +
                "<script src=\"http://localhost:8081/scripts/UniqueElementList.js\"></script>\n" +
                "<script src=\"http://localhost:8081/lib/ajaxhook.js\"></script>\n" +
                "<link href=\"http://localhost:8081/lib/bootstrap-3.3.7/css/bootstrap.css\" rel=\"stylesheet\"/>\n" +
                "<link href=\"http://localhost:8081/css/content_script.css\" rel=\"stylesheet\"/>\n" +
                "<link href=\"http://localhost:8081/css/jquery.mloading.css\" rel=\"stylesheet\"/>";
        String position = "(<meta .+?>)";
        return html.replaceFirst(position, "$1 " + js);
    }

    public static void main(String[] args) throws MalformedURLException {

    }
}
