package com.xm.flowable.domain.dto;

import lombok.Data;

@Data
public class VariableData {
    //类型,关联VariableDataTypeEnum枚举
    private String type;
    //key和value
    private String key;
    private Object value;
}
