package com.xm.auth.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 路由操作
 */
public class TcRouterAction implements Serializable {
    /**
    * 主键
    */
    @Size(max = 32,message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
    * 路由id
    */
    @Size(max = 32,message = "路由id最大长度要小于 32")
    @NotBlank(message = "路由id不能为空")
    private String routerId;

    /**
    * 操作编码
    */
    @Size(max = 100,message = "操作编码最大长度要小于 100")
    @NotBlank(message = "操作编码不能为空")
    private String actionCode;

    /**
    * 操作名称
    */
    @Size(max = 100,message = "操作名称最大长度要小于 100")
    private String actionName;

    /**
    * 操作类型0、按钮 1、函数 2、其它
    */
    @Size(max = 100,message = "操作类型0、按钮 1、函数 2、其它最大长度要小于 100")
    private String actionType;

    /**
    * 是否可用
    */
    @NotNull(message = "是否可用不能为null")
    private Integer judgeEnable;

    /**
    * 创建人员
    */
    @Size(max = 50,message = "创建人员最大长度要小于 50")
    private String createUser;

    /**
    * 创建日期
    */
    private Date createDate;

    /**
    * 修改人员
    */
    @Size(max = 50,message = "修改人员最大长度要小于 50")
    private String updateUser;

    /**
    * 修改日期
    */
    private Date updateDate;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getRouterId() {
        return routerId;
    }

    public void setRouterId(String routerId) {
        this.routerId = routerId == null ? null : routerId.trim();
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode == null ? null : actionCode.trim();
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName == null ? null : actionName.trim();
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType == null ? null : actionType.trim();
    }

    public Integer getJudgeEnable() {
        return judgeEnable;
    }

    public void setJudgeEnable(Integer judgeEnable) {
        this.judgeEnable = judgeEnable;
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

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", routerId=").append(routerId);
        sb.append(", actionCode=").append(actionCode);
        sb.append(", actionName=").append(actionName);
        sb.append(", actionType=").append(actionType);
        sb.append(", judgeEnable=").append(judgeEnable);
        sb.append(", createUser=").append(createUser);
        sb.append(", createDate=").append(createDate);
        sb.append(", updateUser=").append(updateUser);
        sb.append(", updateDate=").append(updateDate);
        sb.append("]");
        return sb.toString();
    }
}