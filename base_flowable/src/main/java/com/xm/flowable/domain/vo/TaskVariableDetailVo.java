package com.xm.flowable.domain.vo;

import lombok.Data;

@Data
public class TaskVariableDetailVo {
    private String key;
    private String value;

    public TaskVariableDetailVo(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
