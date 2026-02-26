package com.xm.flowable.enums;

import java.util.HashMap;
import java.util.Map;

public enum TaskStatusEnum {
    init,
    success,
    //中止和拒绝目前没区别
    reject,
    suspend,
    rollback,
    rollBackRecordMsg,
    //转办 有去无回
    transfer,
    //委派 有去有回
    delegate,
    delete;

    private static final Map<String,TaskStatusEnum> mapping=new HashMap<>();

    public static TaskStatusEnum getEnumByName(String name){
        return mapping.get(name);
    }

    static {
        for (TaskStatusEnum statusEnum:TaskStatusEnum.values()){
            mapping.put(statusEnum.name(),statusEnum);
        }
    }
}
