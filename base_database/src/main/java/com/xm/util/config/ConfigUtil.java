package com.xm.util.config;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.module.config.domain.entity.TcConfig;
import com.xm.module.config.mapper.TcConfigMapper;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.id.SnowIdUtil;

import java.util.Date;
import java.util.List;

public class ConfigUtil {
    private static final TcConfigMapper configMapper;

    static {
        configMapper = SpringBeanUtil.getBeanByClass(TcConfigMapper.class);
    }


    public static List<TcConfig> getGroupConfigList(String group){
        LambdaQueryWrapper<TcConfig> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TcConfig::getGroupCode,group);
        return configMapper.selectList(queryWrapper);
    }

    public static TcConfig getConfig(String group, String code){
        LambdaQueryWrapper<TcConfig> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TcConfig::getGroupCode,group).eq(TcConfig::getConfigCode,code);
        return configMapper.selectOne(queryWrapper);
    }

    public static void saveConfigList(List<TcConfig> configList){
        for (TcConfig config : configList) {
            saveConfig(config);
        }
    }

    public static void saveConfig(TcConfig tcConfig){
        if (StrUtil.isBlank(tcConfig.getGroupCode())){
            throw new CommonException("配置组编码为空");
        }
        if (StrUtil.isBlank(tcConfig.getConfigCode())){
            throw new CommonException("配置编码为空");
        }
        TcConfig dataBaseConfig=getConfig(tcConfig.getGroupCode(),tcConfig.getConfigCode());
        Date now = new Date();
        if (dataBaseConfig==null){
            tcConfig.setCreateDate(now);
            tcConfig.setCreateUser("system");
            tcConfig.setId(SnowIdUtil.getSnowId());
            configMapper.insert(tcConfig);
        }else {
            dataBaseConfig.setConfigValue(tcConfig.getConfigValue());
            configMapper.updateById(dataBaseConfig);
        }
    }


    public static void saveConfig(String groupCode, String code, String value){
        TcConfig config=new TcConfig();
        config.setGroupCode(groupCode);
        config.setConfigCode(code);
        config.setConfigValue(value);
        saveConfig(config);
    }

    public static void deleteGroupConfig(String groupCode){
        LambdaQueryWrapper<TcConfig> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcConfig::getGroupCode,groupCode);
        configMapper.delete(lambdaQueryWrapper);
    }

    public static void deleteConfig(String groupCode, String code){
        LambdaQueryWrapper<TcConfig> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TcConfig::getGroupCode,groupCode)
                .eq(TcConfig::getConfigCode,code);
        configMapper.delete(lambdaQueryWrapper);
    }
}
