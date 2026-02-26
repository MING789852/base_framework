package com.xm.flowable.domain.vo;

import lombok.Data;

@Data
public class ProcessVariableVo {
    private String key;
    private String value;

    public ProcessVariableVo(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
