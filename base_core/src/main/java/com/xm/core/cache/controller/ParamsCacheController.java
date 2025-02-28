package com.xm.core.cache.controller;

import com.xm.core.cache.paramsCache.service.ParamsCacheService;
import com.xm.core.params.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("paramsCache")
@RequiredArgsConstructor
public class ParamsCacheController {
    private final ParamsCacheService paramsCacheService;

    @PostMapping("setParamsCache")
    public Result<String> setParamsCache(@RequestBody Object value){
        return Result.successForData(paramsCacheService.setParamsCache(value));
    }
}
