package com.xm.core.cache.paramsCache.service.impl;

import com.xm.core.cache.paramsCache.service.ParamsCacheService;
import com.xm.util.cache.ParamsCacheUtil;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ParamsCacheServiceImpl implements ParamsCacheService {

    @Override
    public String setParamsCache(Object value) {
        //参数在一分钟后过期
        return ParamsCacheUtil.setParamsCache(value,1L, TimeUnit.MINUTES);
    }
}
