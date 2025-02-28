package com.xm.auth.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 角色和路由操作关联表
 */
public class TcRoleRouterActionRel implements Serializable {
    /**
     * 主键
     */
    @Size(max = 32, message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
     * 路由操作主键
     */
    @Size(max = 32, message = "路由操作主键最大长度要小于 32")
    @NotBlank(message = "路由操作主键不能为空")
    private String routerActionId;

    /**
     * 角色主键
     */
    @Size(max = 32, message = "角色主键最大长度要小于 32")
    @NotBlank(message = "角色主键不能为空")
    private String roleId;

    /**
     * 是否可用
     */
    @NotNull(message = "是否可用不能为null")
    private Integer judgeEnable;

    /**
     * 创建人员
     */
    @Size(max = 50, message = "创建人员最大长度要小于 50")
    private String createUser;

    /**
     * 创建日期
     */
    private Date createDate;

    /**
     * 修改人员
     */
    @Size(max = 50, message = "修改人员最大长度要小于 50")
    private String updateUser;

    /**
     * 修改日期
     */
    private Date updateDate;

    /**
     * 路由主键
     */
    @Size(max = 32, message = "路由主键最大长度要小于 32")
    @NotBlank(message = "路由主键不能为空")
    private String routerId;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getRouterActionId() {
        return routerActionId;
    }

    public void setRouterActionId(String routerActionId) {
        this.routerActionId = routerActionId == null ? null : routerActionId.trim();
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId == null ? null : roleId.trim();
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

    public String getRouterId() {
        return routerId;
    }

    public void setRouterId(String routerId) {
        this.routerId = routerId == null ? null : routerId.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", routerActionId=").append(routerActionId);
        sb.append(", roleId=").append(roleId);
        sb.append(", judgeEnable=").append(judgeEnable);
        sb.append(", createUser=").append(createUser);
        sb.append(", createDate=").append(createDate);
        sb.append(", updateUser=").append(updateUser);
        sb.append(", updateDate=").append(updateDate);
        sb.append(", routerId=").append(routerId);
        sb.append("]");
        return sb.toString();
    }
}