package com.xm.flowable.domain.vo;

import lombok.Data;

import java.util.Map;

@Data
public class FlowableExecuteVo {
    private Map<String,String> taskIdAndUserNameMapping;
    private Map<String,String> taskIdAndUserIdMapping;
    private Map<String,String> allowRollBackIdAndNameMapping;
    private Map<String, String> userIdAndNameMapping;
}
