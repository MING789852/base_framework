package com.xm.module.mappingStringConfig.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.cache.config.CustomCacheConfig;
import com.xm.module.mappingStringConfig.consts.MappingStringConfigConst;
import com.xm.module.mappingStringConfig.domain.vo.MappingStringConfig;
import com.xm.module.mappingStringConfig.service.MappingStringConfigService;
import com.xm.module.config.domain.entity.TcConfig;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.config.ConfigUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MappingStringConfigServiceImpl implements MappingStringConfigService {
    @Override
    @Cacheable(value = MappingStringConfigConst.cacheConfigKey,keyGenerator = CustomCacheConfig.keyGeneratorName)
    public List<MappingStringConfig> getConfig(String configKey,List<String> nameList) {
        if (StrUtil.isBlank(configKey)){
            throw new CommonException("configKey不能为空");
        }
        TcConfig config = ConfigUtil.getConfig(MappingStringConfigConst.configGroupName, configKey);
        if (config!=null&& StrUtil.isNotBlank(config.getConfigValue())){
            return JSONUtil.toList(config.getConfigValue(), MappingStringConfig.class);
        }else {
            if (CollectionUtil.isEmpty(nameList)){
                log.error("nameList为空,返回空配置");
                return new ArrayList<>();
            }
            List<MappingStringConfig> configList=new ArrayList<>();
            for (String name : nameList){
                MappingStringConfig groupConfig = new MappingStringConfig();
                groupConfig.setName(name);
                groupConfig.setValue("");
                configList.add(groupConfig);
            }
            return configList;
        }
    }

    @Override
    @CacheEvict(value = MappingStringConfigConst.cacheConfigKey,allEntries = true)
    public String saveConfig(String configKey,List<MappingStringConfig> list) {
        if (StrUtil.isBlank(configKey)){
            throw new CommonException("configKey不能为空");
        }
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空，无需保存");
        }
        ConfigUtil.saveConfig(MappingStringConfigConst.configGroupName,configKey,JSONUtil.toJsonStr(list));
        return "操作成功";
    }

    @Override
    public Map<String, String> getConfigMap(String configKey, List<String> nameList) {
        MappingStringConfigService beanByClass = SpringBeanUtil.getBeanByClass(MappingStringConfigService.class);
        List<MappingStringConfig> config = beanByClass.getConfig(configKey, nameList);
        if (config==null){
            return new HashMap<>();
        }else {
            return config.stream().collect(Collectors.toMap(MappingStringConfig::getName, MappingStringConfig::getValue));
        }
    }


    @Override
    public String getConfigValue(String configKey, String name) {
        if (StrUtil.isBlank(configKey)){
            return null;
        }
        if (StrUtil.isBlank(name)){
            return null;
        }
        MappingStringConfigService beanByClass = SpringBeanUtil.getBeanByClass(MappingStringConfigService.class);
        List<MappingStringConfig> configList = beanByClass.getConfig(configKey,null);
        for (MappingStringConfig config : configList) {
            if (StrUtil.equals(config.getName(), name)){
                return config.getValue();
            }
        }
        return null;
    }
}
