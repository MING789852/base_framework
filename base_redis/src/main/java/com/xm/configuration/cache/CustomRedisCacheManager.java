package com.xm.configuration.cache;

import cn.hutool.core.util.StrUtil;
import com.xm.util.cache.CacheConfigUtil;
import com.xm.util.cache.params.ExpireParams;
import lombok.NonNull;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;

public class CustomRedisCacheManager extends RedisCacheManager {
    public CustomRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }


    /**
     *
     * @param name 原来名称只作为redis存储键值，现重写为可通过#拼接时间
     *             1、如果没有#则默认1h，如果是#u代表不设置过期时间
     *             2、时间单位 d(天) h(小时) m(分钟) s(秒)
     *             3、
     *             示例getData#2h,即2小时重新获取一次数据
     *             示例getData#u,即不设置过期时间一直缓存
     * @param cacheConfig cache对应的配置
     * @return RedisCache
     */
    @Override
    @NonNull
    protected RedisCache createRedisCache(@NonNull String name, RedisCacheConfiguration cacheConfig) {
        String ttlWithUnit = "";
        final int lastIndexOf = StrUtil.lastIndexOfIgnoreCase(name, "#");
        if (lastIndexOf > -1 && lastIndexOf != name.length() - 1) {
            ttlWithUnit = name.substring(lastIndexOf + 1).toLowerCase();
            //去掉后缀
            name = name.substring(0, lastIndexOf);
        }
        ExpireParams expireParams = CacheConfigUtil.getExpireParams(ttlWithUnit);

        if (cacheConfig!=null) {
            long ttl = expireParams.getTtl();
            String unit = expireParams.getUnit();
            Duration duration = expireParams.getDuration();
            cacheConfig=cacheConfig
                    .prefixCacheNameWith(StrUtil.format("cache:{}:",ttl+unit))
                    .entryTtl(duration);
        }
        return super.createRedisCache(name, cacheConfig);
    }
}
