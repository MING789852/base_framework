package com.xm.module.mappingUserConfig.service;

import com.xm.auth.domain.entity.TcUser;
import com.xm.module.mappingUserConfig.domain.vo.MappingUserConfig;

import java.util.List;
import java.util.Map;

public interface MappingUserConfigService {
    //获取配置
    List<MappingUserConfig> getConfig(String configKey,List<String> nameList);

    String saveConfig(String configKey, List<MappingUserConfig> list);

    Map<String,List<TcUser>> getConfigMap(String configKey, List<String> nameList);

    List<TcUser> getConfigUser(String configKey, String name);
}
