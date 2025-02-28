package com.xm.flowable.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FlowableResourceBpmnDeployment {
    @NotBlank(message = "资源名称不能为空")
    private String resourceName;
    @NotBlank(message = "key不能为空")
    private String processDefinitionKey;
    @NotBlank(message = "name不能为空")
    private String name;
    @NotNull(message = "是否覆盖标识不能为空")
    private boolean cover = false;
}
