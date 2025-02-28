package com.xm.flowable.domain.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
public class SimpleFlowUser {
    @NotBlank(message = "审批人员不能为空不能为空")
    private String assignee;
    @NotBlank(message = "节点名称不能为空")
    private String name;

    public SimpleFlowUser(String assignee,String name) {
        this.name = name;
        this.assignee = assignee;
    }

    public SimpleFlowUser() {
    }
}
