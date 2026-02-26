package com.xm.flowable.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateTestProcessInstance {
    private String processDefinitionKey;
    private String businessKey;
    //类监听器
    private String listenerClassName;
    private List<VariableData> variableDataList;
}
