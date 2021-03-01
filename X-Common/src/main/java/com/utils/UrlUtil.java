package com.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtil {
    static Pattern URL_PAT = Pattern.compile("^(http(s)?:\\/\\/)?([\\w-]+\\.)+[\\w-]+(:\\d{2,5})?(\\/?\\#?[\\w-.\\/?%&=]*)?$");

    /**
     * 验证url地址是否合法
     *
     * @param url
     * @return
     */
    public static boolean checkUrl(String url) {
        if (url == null) return false;
        Matcher matcher = URL_PAT.matcher(url);
        return matcher.matches();
    }

}
