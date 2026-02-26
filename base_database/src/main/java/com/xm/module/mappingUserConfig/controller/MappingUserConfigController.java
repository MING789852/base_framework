package com.xm.module.mappingUserConfig.controller;

import com.xm.core.params.Result;
import com.xm.module.mappingUserConfig.domain.vo.MappingUserConfig;
import com.xm.module.mappingUserConfig.service.MappingUserConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("mappingUserConfig")
@RequiredArgsConstructor
public class MappingUserConfigController {
    private final MappingUserConfigService configService;

    @PostMapping("getConfig")
    public Result<List<MappingUserConfig>> getConfig(String configKey,@RequestBody List<String> nameList) {
        return Result.successForData(configService.getConfig(configKey,nameList));
    }

    @PostMapping("saveConfig")
    public Result<String> saveConfig(String configKey,@RequestBody List<MappingUserConfig> list) {
        return Result.successForData(configService.saveConfig(configKey,list));
    }
}
