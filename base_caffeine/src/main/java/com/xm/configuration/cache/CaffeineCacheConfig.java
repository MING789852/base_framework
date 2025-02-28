package com.xm.configuration.cache;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.xm.core.cache.config.CustomCacheConfig;
import com.xm.util.cache.CacheConfigUtil;
import com.xm.util.cache.params.ExpireParams;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CaffeineCacheConfig {


    @Bean(CustomCacheConfig.keyGeneratorName)
    public KeyGenerator keyGenerator(){
        return new KeyGenerator() {
            /**
             *
             * @param target  类对象
             * @param method  方法
             * @param params 参数
             */
            @NonNull
            @Override
            public Object generate(@NonNull Object target, @NonNull Method method, @NonNull Object... params) {
                String className = target.getClass().getName();
                String methodName = method.getName();
                List<Object> paramList=Arrays.stream(params).collect(Collectors.toList());
                paramList.add(className);
                paramList.add(methodName);
                String jsonStr=JSONUtil.toJsonStr(paramList);
                return DigestUtil.md5Hex(jsonStr);
            }
        };
    }


    @Bean(CustomCacheConfig.cacheManager)
    @ConditionalOnProperty(name = "cache.type", havingValue = "CAFFEINE",matchIfMissing = true)
    public CacheManager defaultCacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // 如果使用的是弱引用键或值（通过.weakKeys()或.weakValues()），即使设置了不过期，这些引用仍然可以因为垃圾回收而被清除。确保使用场景不需要这些弱引用特性。
        // 配置缓存及其过期时间
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfter(new Expiry<Object,Object>() {
                    //在KV被创建时触发
                    @Override
                    public long expireAfterCreate(@NonNull Object key,@NonNull Object value, long currentTime) {
                        return getMaxInterval(key);
                    }

                    //在KV被更新时触发
                    @Override
                    public long expireAfterUpdate(@NonNull Object key, @NonNull Object value, long currentTime, @NonNegative long currentDuration) {
                        return getMaxInterval(key);
                    }

                    //在KV被读取时触发
                    @Override
                    public long expireAfterRead(@NonNull Object key, @NonNull Object value, long currentTime, @NonNegative long currentDuration) {
                        return currentDuration;
                    }

                    private long getMaxInterval(Object keyObj) {
                        String key = keyObj.toString();
                        String ttlWithUnit = "";
                        final int lastIndexOf = StrUtil.lastIndexOfIgnoreCase(key, "#");
                        if (lastIndexOf > -1 && lastIndexOf != key.length() - 1) {
                            ttlWithUnit = key.substring(lastIndexOf + 1).toLowerCase();
                        }
                        ExpireParams expireParams = CacheConfigUtil.getExpireParams(ttlWithUnit);
                        Duration duration = expireParams.getDuration();

                        return duration.toNanos();
                    }
                }));
        return cacheManager;
    }
}
