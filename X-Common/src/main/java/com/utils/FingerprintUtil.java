package com.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class FingerprintUtil {
    //用来计算 每个任务中单个新闻的hash,防止重复数据库

    public static String MD5Url(String url) {
        //最简单的手指,直接用url算个md5,然后存入redis
        return DigestUtils.md5Hex(url.trim());
    }


}
