package com.xm.auth.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 用户和部门关联表
 */
public class TcUserDeptRel implements Serializable {
    /**
    * 主键
    */
    @Size(max = 32,message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
    * 用户主键
    */
    @Size(max = 32,message = "用户主键最大长度要小于 32")
    @NotBlank(message = "用户主键不能为空")
    private String userId;

    /**
    * 部门主键
    */
    @Size(max = 32,message = "部门主键最大长度要小于 32")
    @NotBlank(message = "部门主键不能为空")
    private String deptId;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId == null ? null : deptId.trim();
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
}