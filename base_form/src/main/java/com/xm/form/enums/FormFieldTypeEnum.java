package com.xm.form.enums;

import lombok.Getter;

@Getter
public enum FormFieldTypeEnum {
    INPUT("INPUT","输入框"),
    INPUT_AREA("INPUT_AREA","文本框"),
    DATE("DATE","日期"),
    IMAGE("IMAGE","图片上传"),
    FILE("FILE","文件上传"),
    LABEL("LABEL","标签"),
    TREE_SELECT("TREE_SELECT","树状选择"),
    COMMON_SELECT("COMMON_SELECT","普通选择");

    private final String value;
    private final String label;
    FormFieldTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
