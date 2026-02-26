package com.xm.module.mappingStringConfig.controller;

import com.xm.core.params.Result;
import com.xm.module.mappingStringConfig.domain.vo.MappingStringConfig;
import com.xm.module.mappingStringConfig.service.MappingStringConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("mappingStringConfig")
@RequiredArgsConstructor
public class MappingStringConfigController {
    private final MappingStringConfigService mappingStringConfigService;

    @PostMapping("getConfig")
    public Result<List<MappingStringConfig>> getConfig(String configKey,@RequestBody(required = false) List<String> nameList) {
        return Result.successForData(mappingStringConfigService.getConfig(configKey,nameList));
    }

    @PostMapping("saveConfig")
    public Result<String> saveConfig(String configKey,@RequestBody List<MappingStringConfig> list) {
        return Result.successForData(mappingStringConfigService.saveConfig(configKey,list));
    }
}
