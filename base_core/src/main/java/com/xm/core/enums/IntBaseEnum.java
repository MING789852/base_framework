package com.xm.core.enums;

import lombok.Getter;

@Getter
public enum IntBaseEnum implements IBaseEnum<Integer> {
    YES(1,"是"),
    NO(0,"否");


    private final Integer value;

    private final String label;

    IntBaseEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
