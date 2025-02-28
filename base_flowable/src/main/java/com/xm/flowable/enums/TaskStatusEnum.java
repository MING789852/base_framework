package com.xm.flowable.enums;

import java.util.HashMap;
import java.util.Map;

public enum TaskStatusEnum {
    init,
    success,
    reject,
    suspend,
    rollback,
    transfer,
    delete;

    public static final Map<String,TaskStatusEnum> mapping=new HashMap<>();

    static {
        for (TaskStatusEnum statusEnum:TaskStatusEnum.values()){
            mapping.put(statusEnum.name(),statusEnum);
        }
    }
}
