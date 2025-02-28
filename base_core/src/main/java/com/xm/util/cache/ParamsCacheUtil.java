package com.xm.util.cache;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ParamsCacheUtil {

    public static String setParamsCache(Object value, Long timeout, TimeUnit unit){
        String key = StrUtil.format("{}:{}","paramsCache",IdUtil.fastUUID());
        if (timeout==null||unit==null){
            CacheUtil.set(key,value,3, TimeUnit.MINUTES);
        }else {
            CacheUtil.set(key,value,timeout,unit);
        }
        return key;
    }

    public static <T> T getParamsCacheObject(String key,Class<T> clazz){
        Object object = CacheUtil.get(key);
        if (object==null){
            return null;
        }
        return BeanUtil.toBean(object, clazz);
    }

    public static <T> List<T> getParamsCacheList(String key, Class<T> clazz){
        Object object = CacheUtil.get(key);
        if (object==null){
            return null;
        }
        return JSONUtil.toList(JSONUtil.parseArray(object), clazz);
    }
}
