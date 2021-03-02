package com.utils;

public class RedisConstant {
    public static final String REDIS_CMD_PREFIX = "cmd_";
    public static final String REDIS_STATE_PREFIX = "state_";
    //每个任务在redis中的新闻集合key前缀
    public static final String HASH_PREFIX_KEY = "task_article_hash";

    public static String getHashKey(String taskId) {
        return HASH_PREFIX_KEY + taskId;
    }

    public static String getCmdKey(String nodeId) {
        return REDIS_CMD_PREFIX + nodeId;
    }

    public static String getStateKey(String nodeId) {
        return REDIS_STATE_PREFIX + nodeId;
    }


}
