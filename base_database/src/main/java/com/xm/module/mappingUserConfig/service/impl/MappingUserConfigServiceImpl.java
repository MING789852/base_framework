package com.xm.module.mappingUserConfig.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.core.cache.config.CustomCacheConfig;
import com.xm.module.config.domain.entity.TcConfig;
import com.xm.module.mappingUserConfig.consts.MappingUserConfigConst;
import com.xm.module.mappingUserConfig.domain.vo.MappingUserConfig;
import com.xm.module.mappingUserConfig.service.MappingUserConfigService;
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
public class MappingUserConfigServiceImpl implements MappingUserConfigService {

    @Override
    @Cacheable(value = MappingUserConfigConst.cacheConfigKey,keyGenerator = CustomCacheConfig.keyGeneratorName)
    public List<MappingUserConfig> getConfig(String configKey,List<String> nameList){
        if (StrUtil.isBlank(configKey)){
            throw new CommonException("configKey不能为空");
        }
        TcConfig config = ConfigUtil.getConfig(MappingUserConfigConst.configGroupName, configKey);
        if (config!=null&& StrUtil.isNotBlank(config.getConfigValue())){
            return JSONUtil.toList(config.getConfigValue(), MappingUserConfig.class);
        }else {
            if (CollectionUtil.isEmpty(nameList)){
                log.error("nameList为空,返回空配置");
                return new ArrayList<>();
            }
            List<MappingUserConfig> configList=new ArrayList<>();
            for (String name : nameList){
                MappingUserConfig data = new MappingUserConfig();
                data.setName(name);
                data.setUserList(new ArrayList<>());
                configList.add(data);
            }
            return configList;
        }
    }

    @Override
    @CacheEvict(value = MappingUserConfigConst.cacheConfigKey,allEntries = true)
    public String saveConfig(String configKey, List<MappingUserConfig> list) {
        if (StrUtil.isBlank(configKey)){
            throw new CommonException("configKey不能为空");
        }
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空，无需保存");
        }
        ConfigUtil.saveConfig(MappingUserConfigConst.configGroupName,configKey,JSONUtil.toJsonStr(list));
        return "操作成功";
    }

    @Override
    public Map<String, List<TcUser>> getConfigMap(String configKey, List<String> nameList) {
        MappingUserConfigService beanByClass = SpringBeanUtil.getBeanByClass(MappingUserConfigService.class);
        List<MappingUserConfig> config = beanByClass.getConfig(configKey, nameList);
        if (config==null){
            return new HashMap<>();
        }

        return config.stream().collect(Collectors.toMap(MappingUserConfig::getName, MappingUserConfig::getUserList));
    }


    @Override
    public List<TcUser> getConfigUser(String configKey, String name) {
        if (StrUtil.isBlank(configKey)){
            return new ArrayList<>();
        }
        if (StrUtil.isBlank(name)){
            return new ArrayList<>();
        }
        MappingUserConfigService beanByClass = SpringBeanUtil.getBeanByClass(MappingUserConfigService.class);
        List<MappingUserConfig> configList = beanByClass.getConfig(configKey,null);
        for (MappingUserConfig config : configList) {
            if (StrUtil.equals(config.getName(), name)){
                return config.getUserList();
            }
        }
        return new ArrayList<>();
    }
}
