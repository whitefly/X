package spider.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.StringUtil;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TimeUtil {
    //提取发布时间的正则表达式列表
    public static String[] PUBLISH_TIME_RE = {
            "(\\d{4}[-|/|.]\\d{1,2}[-|/|.]\\d{1,2}\\s*?[0-1]?[0-9]:[0-5]?[0-9]:[0-5]?[0-9])",
            "(\\d{4}[-|/|.]\\d{1,2}[-|/|.]\\d{1,2}\\s*?[2][0-3]:[0-5]?[0-9]:[0-5]?[0-9])",
            "(\\d{4}[-|/|.]\\d{1,2}[-|/|.]\\d{1,2}\\s*?[0-1]?[0-9]:[0-5]?[0-9])",
            "(\\d{4}[-|/|.]\\d{1,2}[-|/|.]\\d{1,2}\\s*?[2][0-3]:[0-5]?[0-9])",
            "(\\d{4}[-|/|.]\\d{1,2}[-|/|.]\\d{1,2}\\s*?[1-24]\\d时[0-60]\\d分)([1-24]\\d时)",
            "(\\d{2}[-|/|.]\\d{1,2}[-|/|.]\\d{1,2}\\s*?[0-1]?[0-9]:[0-5]?[0-9]:[0-5]?[0-9])",
            "(\\d{2}[-|/|.]\\d{1,2}[-|/|.]\\d{1,2}\\s*?[2][0-3]:[0-5]?[0-9]:[0-5]?[0-9])",
            "(\\d{2}[-|/|.]\\d{1,2}[-|/|.]\\d{1,2}\\s*?[0-1]?[0-9]:[0-5]?[0-9])",
            "(\\d{2}[-|/|.]\\d{1,2}[-|/|.]\\d{1,2}\\s*?[2][0-3]:[0-5]?[0-9])",
            "(\\d{2}[-|/|.]\\d{1,2}[-|/|.]\\d{1,2}\\s*?[1-24]\\d时[0-60]\\d分)([1-24]\\d时)",
            "(\\d{4}年\\d{1,2}月\\d{1,2}日\\s*?[0-1]?[0-9]:[0-5]?[0-9]:[0-5]?[0-9])",
            "(\\d{4}年\\d{1,2}月\\d{1,2}日\\s*?[2][0-3]:[0-5]?[0-9]:[0-5]?[0-9])",
            "(\\d{4}年\\d{1,2}月\\d{1,2}日\\s*?[0-1]?[0-9]:[0-5]?[0-9])",
            "(\\d{4}年\\d{1,2}月\\d{1,2}日\\s*?[2][0-3]:[0-5]?[0-9])",
            "(\\d{4}年\\d{1,2}月\\d{1,2}日\\s*?[1-24]\\d时[0-60]\\d分)([1-24]\\d时)",
            "(\\d{2}年\\d{1,2}月\\d{1,2}日\\s*?[0-1]?[0-9]:[0-5]?[0-9]:[0-5]?[0-9])",
            "(\\d{2}年\\d{1,2}月\\d{1,2}日\\s*?[2][0-3]:[0-5]?[0-9]:[0-5]?[0-9])",
            "(\\d{2}年\\d{1,2}月\\d{1,2}日\\s*?[0-1]?[0-9]:[0-5]?[0-9])",
            "(\\d{2}年\\d{1,2}月\\d{1,2}日\\s*?[2][0-3]:[0-5]?[0-9])",
            "(\\d{2}年\\d{1,2}月\\d{1,2}日\\s*?[1-24]\\d时[0-60]\\d分)([1-24]\\d时)",
            "(\\d{1,2}月\\d{1,2}日\\s*?[0-1]?[0-9]:[0-5]?[0-9]:[0-5]?[0-9])",
            "(\\d{1,2}月\\d{1,2}日\\s*?[2][0-3]:[0-5]?[0-9]:[0-5]?[0-9])",
            "(\\d{1,2}月\\d{1,2}日\\s*?[0-1]?[0-9]:[0-5]?[0-9])",
            "(\\d{1,2}月\\d{1,2}日\\s*?[2][0-3]:[0-5]?[0-9])",
            "(\\d{1,2}月\\d{1,2}日\\s*?[1-24]\\d时[0-60]\\d分)([1-24]\\d时)",
            "(\\d{4}[-|/|.]\\d{1,2}[-|/|.]\\d{1,2})",
            "(\\d{2}[-|/|.]\\d{1,2}[-|/|.]\\d{1,2})",
            "(\\d{4}年\\d{1,2}月\\d{1,2}日)",
            "(\\d{2}年\\d{1,2}月\\d{1,2}日)",
            "(\\d{1,2}月\\d{1,2}日)"
    };
    //时间提取工具
    static String FULL_PUBLISH_RE = String.join("|", PUBLISH_TIME_RE);
    static Pattern compile = Pattern.compile(FULL_PUBLISH_RE);

    public static String extractTimeStr(String content) {
        if (!StringUtils.isEmpty(content)) {
            Matcher matcher = compile.matcher(content);
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return "";
    }

    public static Date convert2Date(String dateStr) {
        if (StringUtil.isBlank(dateStr)) {
            return null;
        }
        String parse = dateStr;
        DateFormat format;

        //识别出年份 2012年 2013/ 这样的格式,
        parse = parse.replaceFirst("^(18|19|20|21|22){1}[0-9]{2}([^0-9]?)", "yyyy$2");
//        parse = parse.replaceFirst("^[0-9]{2}([^0-9]?)", "yy$1");
        if (!parse.contains("yyyy")) {
            //不包含年份,则直接判定为解析错误
            return null;
        }
        parse = parse.replaceFirst("([^0-9]?)(1{1}[0-2]{1}|0?[1-9]{1})([^0-9]?)", "$1MM$3");
        parse = parse.replaceFirst("([^0-9]?)(3{1}[0-1]{1}|[0-2]?[0-9]{1})([^0-9]?)", "$1dd$3");
        parse = parse.replaceFirst("([^0-9]?)(2[0-3]{1}|[0-1]?[0-9]{1})([^0-9]?)", "$1HH$3");
        parse = parse.replaceFirst("([^0-9]?)[0-5]?[0-9]{1}([^0-9]?)", "$1mm$2");
        parse = parse.replaceFirst("([^0-9]?)[0-5]?[0-9]{1}([^0-9]?)", "$1ss$2");
        try {
            format = new SimpleDateFormat(parse);
            //设置为严格验证时间格式，默认是非严格的，1月32不会报错，会解析为2月1号。
            format.setLenient(true);
            Date date = format.parse(dateStr);
            log.debug(String.format("原始字符串：%s,判断格式：%s,解析结果：%s", dateStr, parse, date.toString()));
            return date;
        } catch (Exception e) {
            log.error(String.format("日期解析出错：%s-->%s", parse, dateStr));
            log.debug(null, e);
        }
        return null;
    }

}
