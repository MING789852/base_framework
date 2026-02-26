package com.xm.module.mappingStringConfig.service;

import com.xm.module.mappingStringConfig.domain.vo.MappingStringConfig;

import java.util.List;
import java.util.Map;

public interface MappingStringConfigService {
    List<MappingStringConfig> getConfig(String configKey,List<String> nameList);

    String saveConfig(String configKey, List<MappingStringConfig> list);

    Map<String, String> getConfigMap(String configKey, List<String> nameList);

    String getConfigValue(String configKey, String name);
}
