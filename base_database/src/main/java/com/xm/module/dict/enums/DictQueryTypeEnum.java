package com.xm.module.dict.enums;

import lombok.Getter;

@Getter
public enum DictQueryTypeEnum {
    TREE("TREE","树状选择"),
    COMMON("COMMON","普通选择"),
    DEPARTMENT_TREE("DEPARTMENT_TREE","[部门]树状选择"),
    DEPARTMENT_COMMON("DEPARTMENT_COMMON","[部门]普通选择"),
    USER_COMMON("USER_COMMON","[用户]普通选择");

    private final String value;
    private final String label;
    DictQueryTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
