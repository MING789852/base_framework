package com.xm.util.cache;


import com.xm.advice.exception.exception.CommonException;
import com.xm.core.cache.CacheService;
import com.xm.util.bean.SpringBeanUtil;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CacheUtil {

    private final static CacheService cacheService;

    static {
        cacheService = SpringBeanUtil.getBeanByClass(CacheService.class);
    }


    public static Object get(String key){
        return cacheService.get(key);
    }

    public static void set(String key, Object value){
        cacheService.set(key,value);
    }

    public static void set(String key, Object value, long timeout, TimeUnit unit){
        cacheService.set(key,value,timeout,unit);
    }

    public static void delete(String key){
        cacheService.delete(key);
    }

    public static Set<String> keys(String pattern){
        return cacheService.keys(pattern);
    }

    public static boolean hasKey(String key){
        return !cacheService.keys(key).isEmpty();
    }

    public static void put(String key, String hasKey, Object value){
        cacheService.put(key,hasKey,value);
    }

    public static Object get(String key, String hasKey){
        return cacheService.get(key,hasKey);
    }
}
