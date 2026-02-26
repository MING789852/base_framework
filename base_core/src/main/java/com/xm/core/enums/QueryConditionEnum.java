package com.xm.core.enums;

import lombok.Getter;

@Getter
public enum QueryConditionEnum {
    //精确查询，转化为sql就是x=?
    EQ("eq","eq"),
    //模糊查询，转化为sql就是x like ?
    LIKE("like","like"),
    //批量精确查询，转化为sql就是x not in (?,?,?)
    NOT_IN("notIn","notIn"),
    //批量精确查询，转化为sql就是x in (?,?,?)
    IN("in","in"),
    //批量模糊查询，转化为sql就是x like ? or x like ?
    LIKE_IN("likeIn","likeIn"),
    //范围查询条件，转化为sql就是0<x and x<2
    RANGE("range","range"),
    //日期范围查询条件，后端对开始日期和结束日期做了特殊处理，转化为sql就是BETWEEN ? AND ?
    BETWEEN_DATE("betweenDate","betweenDate"),
    //月份范围查询条件，后端对开始日期和结束日期做了特殊处理，转化为sql就是BETWEEN ? AND ?
    BETWEEN_MONTH("betweenMonth","betweenMonth"),
    //日期字符串范围查询条件 yyyy-MM-dd 格式
    BETWEEN_DATE_STR("betweenDateStr","betweenDateStr"),
    //月份字符串范围查询条件 yyyy-MM 格式
    BETWEEN_MONTH_STR("betweenMonthStr","betweenMonthStr");

    private final String label;
    private final String value;

    QueryConditionEnum(String label, String value) {
        this.label = label;
        this.value = value;
    }
}
