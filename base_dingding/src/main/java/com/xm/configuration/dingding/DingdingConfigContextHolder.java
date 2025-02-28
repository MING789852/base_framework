package com.xm.configuration.dingding;

import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.util.bean.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class DingdingConfigContextHolder {

    private static final Map<String,DingdingProperty> dingdingPropertyMap;

    private static final ThreadLocal<String> CONFIG_KEY=ThreadLocal.withInitial(()->"");

    static {
        DingdingConfig beanByClass = SpringBeanUtil.getBeanByClass(DingdingConfig.class);
        dingdingPropertyMap = beanByClass.getDingdingPropertyMap();
    }


    public static void setConfigKey(String configKey){
        CONFIG_KEY.set(configKey);
    }

    public static KeyDingdingProperty getCurrentKeyAndDingdingProperty(){
        String key = CONFIG_KEY.get();
        KeyDingdingProperty keyDingdingProperty=new KeyDingdingProperty();
        if (StrUtil.isBlank(key)){
            Optional<Map.Entry<String, DingdingProperty>> first = dingdingPropertyMap.entrySet().stream().findFirst();
            if (first.isPresent()){
                Map.Entry<String, DingdingProperty> entry = first.get();
                log.info("当前使用钉钉配置KEY->{}",entry.getKey());
                keyDingdingProperty.setKey(entry.getKey());
                keyDingdingProperty.setProperty(entry.getValue());
                return keyDingdingProperty;
            }else {
                throw new CommonException("使用默认值时,未配置钉钉Key");
            }
        }else {
            log.info("当前使用钉钉配置KEY->{}",key);
            DingdingProperty dingdingProperty = dingdingPropertyMap.get(key);
            if (dingdingProperty==null){
                String format = StrUtil.format("钉钉配置KEY->{}不存在", key);
                throw new CommonException(format);
            }
            keyDingdingProperty.setKey(key);
            keyDingdingProperty.setProperty(dingdingProperty);
            return keyDingdingProperty;
        }
    }

    public static void clear(){
        CONFIG_KEY.remove();
    }
}
