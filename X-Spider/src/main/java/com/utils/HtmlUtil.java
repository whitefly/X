package com.utils;

import org.springframework.util.StringUtils;

public class HtmlUtil {
    public static String smartContent(String html) {
        if (StringUtils.isEmpty(html)) return html;
        html = html.replaceAll("(?is)<!DOCTYPE.*?>", "");
        html = html.replaceAll("(?is)<!--.*?-->", "");                // remove html comment
        html = html.replaceAll("(?is)<script.*?>.*?</script>", ""); // remove javascript
        html = html.replaceAll("(?is)<style.*?>.*?</style>", "");   // remove css
        html = html.replaceAll("&.{2,5};|&#.{2,5};", " ");            // remove special char
        html = html.replaceAll("(?is)<.*?>", "");
        return html.trim();
    }
}
