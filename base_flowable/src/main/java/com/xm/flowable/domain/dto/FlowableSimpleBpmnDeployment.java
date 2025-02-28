package com.xm.flowable.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class FlowableSimpleBpmnDeployment {
    @NotEmpty(message = "审批节点不能为空")
    //外层list代表流程分叉
    //内存list代表每个流程阶段
    private List<List<SimpleFlowCreate>> simpleFlowCreateList;
    @NotBlank(message = "processDefinitionKey不能为空")
    private String processDefinitionKey;
    @NotBlank(message = "name不能为空")
    private String name;
    @NotNull(message = "是否覆盖标识不能为空")
    private boolean cover = false;
}
