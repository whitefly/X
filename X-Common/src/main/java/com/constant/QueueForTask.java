package com.constant;

import com.mytype.ParserDOType;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务模板类型--redis任务队列 的映射
 */
public class QueueForTask {
    public static ConcurrentHashMap<String, String> queueForTask = new ConcurrentHashMap<>();

    static {
        queueForTask.put(ParserDOType.PageParser.name(), RedisConstant.DISPATCHER_LONG_TASK_QUEUE_KEY);
        queueForTask.put(ParserDOType.CustomParser.name(), RedisConstant.DISPATCHER_LONG_TASK_QUEUE_KEY);
    }
}
