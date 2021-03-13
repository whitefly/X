package com.constant;

public class RedisConstant {
    //管理节点发送命令 list
    public static final String REDIS_CMD_KEY_PREFIX = "cmd_";

    //抓取节点上传状态 string
    public static final String REDIS_STATE_KEY_PREFIX = "state_";

    //任务在redis中的新闻hash后集合 set
    public static final String ARTICLE_HASH_KEY_PREFIX = "task_article_hash_";

    //task解析过正文的URL合集,用来减少http访问 set
    public static final String TASK_EXTRACTED_URL_KEY_PREFIX = "task_extracted_urls_";


    //任务分发器的任务队列(短任务队列) list
    public static final String DISPATCHER_SHORT_TASK_QUEUE_KEY = "dispatcher_short_task";

    //任务分发器的任务队列(长任务队列) list
    public static final String DISPATCHER_LONG_TASK_QUEUE_KEY = "dispatcher_long_task";

    //任务被用户监控 Map
    public static final String TASK_SUBSCRIBE_USER_KEY_PREFIX = "subscribe_task_";

    public static String getHashKey(String taskId) {
        return ARTICLE_HASH_KEY_PREFIX + taskId;
    }

    public static String getExtractedKey(String taskId) {
        return TASK_EXTRACTED_URL_KEY_PREFIX + taskId;
    }

    public static String getCmdKey(String nodeId) {
        return REDIS_CMD_KEY_PREFIX + nodeId;
    }

    public static String getStateKey(String nodeId) {
        return REDIS_STATE_KEY_PREFIX + nodeId;
    }

    public static String getSubscribeKey(String taskId) {
        return TASK_SUBSCRIBE_USER_KEY_PREFIX + taskId;
    }

}
