package com.utils;

public class Constant {
    //每个任务在redis中的新闻集合key前缀
    public static final String HASH_PREFIX_KEY = "task_article_hash";

    public static String getHashKey(String taskId) {
        return HASH_PREFIX_KEY + taskId;
    }
}
