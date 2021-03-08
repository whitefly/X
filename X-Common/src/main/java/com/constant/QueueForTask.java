package com.constant;

import java.util.concurrent.ConcurrentHashMap;

public class QueueForTask {
    public static ConcurrentHashMap<String, String> queueForTask = new ConcurrentHashMap<>();

    static {
        queueForTask.put("PageParser", RedisConstant.DISPATCHER_LONG_TASK_QUEUE_KEY);
        queueForTask.put("CustomParser", RedisConstant.DISPATCHER_LONG_TASK_QUEUE_KEY);
    }
}
