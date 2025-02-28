package com.xm.core.enums;

import lombok.Getter;

@Getter
public enum QueryColumnTypeEnum {
    AREA_INPUT("大文本查询","AREA_INPUT"),
    INPUT("文本查询","Input"),
    MONTH("月份查询","Month"),
    DATE("日期查询","Date"),
    DATE_RANGE("日期范围查询","DATE_RANGE"),
    YEAR("年份查询","year"),
    OPTION("单选项查询","Option"),
    MULTIPLE_OPTION("多选项查询","MULTIPLE_OPTION"),
    OPTION_LIKE("模糊选项查询","OPTION_LIKE");

    private final String label;
    private final String value;

    QueryColumnTypeEnum(String label, String value) {
        this.label = label;
        this.value = value;
    }
}
