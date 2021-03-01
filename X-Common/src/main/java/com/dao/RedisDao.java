package com.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisDao {
    private static final String QUEUE_KEY = "task_queue";

    @Autowired
    private StringRedisTemplate redisTemplate;


    public String take() {
        ListOperations<String, String> listOp = redisTemplate.opsForList();
        return listOp.rightPop(QUEUE_KEY, Integer.MAX_VALUE, TimeUnit.SECONDS);
    }

    public void put(String key) {
        ListOperations<String, String> listOp = redisTemplate.opsForList();
        listOp.leftPush(QUEUE_KEY, key);
    }

    public void addSet(String key, String value) {
        SetOperations<String, String> setOp = redisTemplate.opsForSet();
        setOp.add(key, value);
    }

    public void delSet(String key) {
        redisTemplate.delete(key);
    }

    public Boolean existInSet(String key, String value) {
        SetOperations<String, String> setOp = redisTemplate.opsForSet();
        return setOp.isMember(key, value);
    }

}
