package com.xm.cache;


import com.xm.core.cache.CacheService;
import com.xm.util.redis.RedisTemplateUtil;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RedisCacheService implements CacheService {
    @Override
    public Object get(String key) {
        ValueOperations<Object, Object> valueOperations = RedisTemplateUtil.getRedisTemplate().opsForValue();
        return valueOperations.get(key);
    }

    @Override
    public void set(String key, Object value) {
        ValueOperations<Object, Object> valueOperations = RedisTemplateUtil.getRedisTemplate().opsForValue();
        valueOperations.set(key,value);
    }

    @Override
    public void delete(String key) {
         RedisTemplateUtil.getRedisTemplate().delete(key);
    }

    @Override
    public Set<String> keys(String pattern) {
        //非阻塞式扫描
//        return RedisTemplateUtil.getRedisTemplate().execute((RedisCallback<Set<String>>) connection -> {
//            Set<String> keys = new HashSet<>();
//            Cursor<byte[]> cursor = connection.scan(
//                    ScanOptions.scanOptions().match(pattern).count(1000).build());
//            while (cursor.hasNext()) {
//                keys.add(new String(cursor.next()));
//            }
//            cursor.close();
//            return keys;
//        });
        //阻塞式获取
        return Objects.requireNonNull(RedisTemplateUtil.getRedisTemplate().keys(pattern)).stream().map(Object::toString).collect(Collectors.toSet());
    }

    @Override
    public void put(String key, String hasKey, Object value) {
        HashOperations<Object, Object, Object> objectObjectObjectHashOperations = RedisTemplateUtil.getRedisTemplate().opsForHash();
        objectObjectObjectHashOperations.put(key,hasKey,value);
    }

    @Override
    public Object get(String key, String hasKey) {
        HashOperations<Object, Object, Object> objectObjectObjectHashOperations = RedisTemplateUtil.getRedisTemplate().opsForHash();
        return objectObjectObjectHashOperations.get(key,hasKey);
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        ValueOperations<Object, Object> valueOperations = RedisTemplateUtil.getRedisTemplate().opsForValue();
        valueOperations.set(key,value,timeout,unit);
    }
}
