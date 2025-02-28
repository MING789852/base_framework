package com.xm.flowable.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 流程模型
 */
public class TcFlowableModel implements Serializable {
    /**
    * 流程定义key
    */
    @Size(max = 100,message = "流程定义key最大长度要小于 100")
    @NotBlank(message = "流程定义key不能为空")
    private String processDefinitionKey;

    /**
    * 流程定义名称
    */
    @Size(max = 100,message = "流程定义名称最大长度要小于 100")
    @NotBlank(message = "流程定义名称不能为空")
    private String processDefinitionName;

    /**
    * 创建人员
    */
    @Size(max = 100,message = "创建人员最大长度要小于 100")
    private String createUser;

    /**
    * 创建日期
    */
    private Date createDate;

    private static final long serialVersionUID = 1L;

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey == null ? null : processDefinitionKey.trim();
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName == null ? null : processDefinitionName.trim();
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", processDefinitionKey=").append(processDefinitionKey);
        sb.append(", processDefinitionName=").append(processDefinitionName);
        sb.append(", createUser=").append(createUser);
        sb.append(", createDate=").append(createDate);
        sb.append("]");
        return sb.toString();
    }
}