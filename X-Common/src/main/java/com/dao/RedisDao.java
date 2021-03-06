package com.dao;

import com.constant.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RedisDao {

    @Autowired
    private StringRedisTemplate redisTemplate;


    public String take(String queueKey) {
        ListOperations<String, String> listOp = redisTemplate.opsForList();
        return listOp.rightPop(queueKey, Integer.MAX_VALUE, TimeUnit.SECONDS);
    }

    public String pop(String queueKey) {
        ListOperations<String, String> listOp = redisTemplate.opsForList();
        return listOp.rightPop(queueKey);
    }

    public void put(String key, String value) {
        ListOperations<String, String> listOp = redisTemplate.opsForList();
        listOp.leftPush(key, value);
    }

    public String getValue(String key) {
        ValueOperations<String, String> valueOp = redisTemplate.opsForValue();
        return valueOp.get(key);
    }

    public void setValue(String key, String value) {
        ValueOperations<String, String> valueOp = redisTemplate.opsForValue();
        valueOp.set(key, value);
    }

    public void setValueOnExpire(String key, String value, long time, TimeUnit timeUnit) {
        ValueOperations<String, String> valueOp = redisTemplate.opsForValue();
        valueOp.set(key, value, time, timeUnit);
    }

    public void addSet(String key, String value) {
        SetOperations<String, String> setOp = redisTemplate.opsForSet();
        setOp.add(key, value);
    }

    public void delMember(String key, String value) {
        SetOperations<String, String> setOp = redisTemplate.opsForSet();
        setOp.remove(key, value);
    }

    public void delSet(String key) {
        redisTemplate.delete(key);
    }


    public Boolean existInSet(String key, String value) {
        SetOperations<String, String> setOp = redisTemplate.opsForSet();
        return setOp.isMember(key, value);
    }

    public Boolean deleteKey(String key) {
        return redisTemplate.delete(key);
    }

    public List<String> getValueBatch(List<String> keys) {
        ValueOperations<String, String> op = redisTemplate.opsForValue();
        return op.multiGet(keys);
    }

    public void addMap(String key, String hKey, String hValue) {
        HashOperations<String, String, String> op = redisTemplate.opsForHash();
        op.put(key, hKey, hValue);
    }

    public void delMap(String key, String hKey) {
        HashOperations<String, String, Object> op = redisTemplate.opsForHash();
        op.delete(key, hKey);
    }

    public String getPair(String key, String hKey) {
        HashOperations<String, String, String> op = redisTemplate.opsForHash();
        return op.get(key, hKey);
    }

    public Map<String, String> getMap(String key) {
        HashOperations<String, String, String> op = redisTemplate.opsForHash();
        return op.entries(key);
    }
}
