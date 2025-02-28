package com.xm.util.redis;


import com.xm.util.bean.SpringBeanUtil;
import lombok.Getter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class RedisTemplateUtil {
    @Getter
    private static final RedisTemplate<Object,Object> redisTemplate;

    static {
        redisTemplate= SpringBeanUtil.getBeanByName("redisTemplate",RedisTemplate.class);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    }

}
