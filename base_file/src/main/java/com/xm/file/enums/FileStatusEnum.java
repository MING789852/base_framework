package com.xm.file.enums;

public enum FileStatusEnum {
    AVAILABLE("1"),
    NOT_AVAILABLE("0");
    private String value;


    FileStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
