package com.xm.core.enums;

import cn.hutool.core.util.ObjectUtil;

import java.util.EnumSet;
import java.util.Objects;

public interface IBaseEnum<T>{
    T getValue();

    String getLabel();

    /**
     * 根据值获取枚举
     */
    static   <T,E extends Enum<E> & IBaseEnum<T>> E getEnumByValue(T value, Class<E> clazz) {
        if (value==null){
            return null;
        }
        EnumSet<E> allEnums = EnumSet.allOf(clazz); // 获取类型下的所有枚举
        return allEnums.stream()
                .filter(e -> ObjectUtil.equal(e.getValue(), value))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据文本标签获取值
     */
    static <T,E extends Enum<E> & IBaseEnum<T>> String getLabelByValue(T value, Class<E> clazz) {
        E matchEnum = getEnumByValue(value, clazz);
        String label = null;
        if (matchEnum != null) {
            label = matchEnum.getLabel();
        }
        return label;
    }
}
