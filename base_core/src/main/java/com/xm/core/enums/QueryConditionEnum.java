package com.xm.core.enums;

import lombok.Getter;

@Getter
public enum QueryConditionEnum {
    EQ("eq","eq"),
    LIKE("like","like"),
    IN("in","in"),
    LIKE_IN("likeIn","likeIn"),
    RANGE("range","range"),
    BETWEEN_DATE("betweenDate","betweenDate");

    private final String label;
    private final String value;

    QueryConditionEnum(String label, String value) {
        this.label = label;
        this.value = value;
    }
}
