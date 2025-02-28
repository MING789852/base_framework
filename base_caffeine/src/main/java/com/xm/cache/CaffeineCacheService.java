package com.xm.cache;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.cache.CacheService;
import com.xm.params.CacheObject;
import com.xm.util.lock.LockUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CaffeineCacheService implements CacheService {

    public final Cache<String, CacheObject<Object>> CAFFEINE = Caffeine.newBuilder()
            .expireAfter(new Expiry<String, CacheObject<?>>() {
                @Override
                public long expireAfterCreate(@NonNull String key, @NonNull CacheObject<?> value, long currentTime) {
                    //将过期时间设置为自定义的过期时间
                    return value.getNanos();
                }

                @Override
                public long expireAfterUpdate(@NonNull String key, @NonNull CacheObject<?> value, long currentTime, @NonNegative long currentDuration) {
                    return value.getNanos();
                }

                @Override
                public long expireAfterRead(@NonNull String key, @NonNull CacheObject<?> value, long currentTime, @NonNegative long currentDuration) {
                    return currentDuration;
                }
            })
            //监听缓存被移除
            .removalListener((key, val, removalCause) -> {
//                long mills = -1;
//                if (val != null) {
//                    mills = val.getNanos();
//                }
//                log.info("【Caffeine】key->{},过期时间->{}ns,原因->{}", key, mills, removalCause);
            })
            .build();

    @Override
    public Object get(String key) {
        CacheObject<Object> cacheObject = CAFFEINE.getIfPresent(key);
        if (cacheObject == null) {
            return null;
        }
        return cacheObject.getData();
    }

    @Override
    public void set(String key, Object value) {
        set(key, value, 5, TimeUnit.MINUTES);
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        CacheObject<Object> cacheObject = new CacheObject<>(value, timeout, unit);
        CAFFEINE.put(key, cacheObject);
    }

    @Override
    public void delete(String key) {
        CAFFEINE.invalidate(key);
    }

    @Override
    public Set<String> keys(String pattern) {
        String patternStr;
        if (!pattern.equals("*")) {
            if (pattern.endsWith("*")) {
                patternStr = pattern.split("\\*")[0] + "(.*)";
            } else {
                patternStr = pattern;
            }
        } else {
            patternStr = ".*";
        }
        Pattern compile = Pattern.compile(patternStr);
        return CAFFEINE.asMap().keySet().stream().filter(key -> compile.matcher(key).matches()).collect(Collectors.toSet());
    }

    @Override
    public void put(String key, String hasKey, Object value) {
        if (key==null||hasKey==null){
            throw new CommonException("【Caffeine】key和hasKey不能为空");
        }
        LockUtil.lock(key, () -> {
            CacheObject<Object> cacheObject = CAFFEINE.getIfPresent(key);
            Map<String,Object> map;
            if (cacheObject == null){
                map= new ConcurrentHashMap<>();
                cacheObject=new CacheObject<>(map,36500,TimeUnit.DAYS);
                CAFFEINE.put(key,cacheObject);
            }else {
                map = BeanUtil.beanToMap(cacheObject.getData());
            }
            map.put(hasKey,value);
            return true;
        }, (e) -> {
            throw new CommonException(StrUtil.format("【Caffeine】错误->{},无法设置hash缓存", ExceptionUtil.stacktraceToString(e)));
        });
    }

    @Override
    public Object get(String key, String hasKey) {
        if (key==null||hasKey==null){
            throw new CommonException("【Caffeine】key和hasKey不能为空");
        }
        return LockUtil.lock(key, () -> {
            CacheObject<Object> cacheObject = CAFFEINE.getIfPresent(key);
            if (cacheObject == null) {
                return null;
            }else {
                Map<String, Object> map = BeanUtil.beanToMap(cacheObject.getData());
                return map.get(hasKey);
            }
        }, (e) -> {
            throw new CommonException(StrUtil.format("【Caffeine】错误->{},无法获取hash缓存", ExceptionUtil.stacktraceToString(e)));
        });
    }
}
