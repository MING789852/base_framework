package com.xm.auth.enums;


import com.xm.core.enums.IBaseEnum;
import lombok.Getter;

@Getter
public enum UserTypeEnum implements IBaseEnum<Integer> {
    SYSTEM(0,"系统创建"),
    DINGDING(1,"钉钉获取"),
    MINIPROGRAM(2,"小程序获取");

    private final Integer value;


    private final String label;

    UserTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

}
