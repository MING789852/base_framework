package com.xm.flowable.domain.dto;

import com.xm.auth.domain.entity.TcUser;
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
    //如果创建者不为空，则使用创建者信息
    //否则使用当前用户信息
    private TcUser creator;
    private Class<? extends FlowableTaskListener> listener;

    //启动审批人员和当前审批人员相同，则直接通过
    private boolean autoExecuteTask=false;
}
