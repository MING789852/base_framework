package com.xm.flowable.domain.dto;

import com.xm.flowable.listener.FlowableTaskListener;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Data
public class StartProcessInstance{
    private String id;
    @NotBlank(message = "模型key不能为空")
    private String processDefinitionKey;
    @NotBlank(message = "流程名称不能为空")
    private String name;
    @NotBlank(message = "业务key不能为空")
    private String businessKey;
    @NotBlank(message = "业务编码不能为空")
    private String businessNo;
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
    @NotBlank(message = "标题不能为空")
    private String title;
    //备注
    private String description;
    @NotBlank(message = "详情跳转地址不能为空")
    private String jumpUrl;
    private Map<String, Object> data;
    private Class<? extends FlowableTaskListener> listener;
}
