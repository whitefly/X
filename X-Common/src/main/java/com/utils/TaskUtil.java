package com.utils;

import java.time.LocalDate;

public class TaskUtil {

    public static String genEpaperUrl(String templateUrl) {
        //根据电子报url模板来生成最新日期的url
        //{YYYY} {MM} {dd}

        LocalDate now = LocalDate.now();
        String year = String.format("%d", now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());

        return templateUrl
                .replace("{YYYY}", year)
                .replace("{MM}", month)
                .replace("{dd}", day);
    }

    public static boolean isEpaperStartUrlValid(String templateUrl) {
        if (templateUrl == null) return false;
        return templateUrl.contains("{YYYY}") && templateUrl.contains("{MM}") && templateUrl.contains("{dd}");
    }
}
