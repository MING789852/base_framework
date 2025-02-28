package com.xm.auth.domain.entity;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 用户
 */
public class TcButton {
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
    * 按钮图标(预留)
    */
    @Size(max = 100,message = "按钮图标(预留)最大长度要小于 100")
    private String buttonIcon;

    /**
    * 按钮编码
    */
    @Size(max = 100,message = "按钮编码最大长度要小于 100")
    private String buttonCode;

    /**
    * 按钮名称
    */
    @Size(max = 100,message = "按钮名称最大长度要小于 100")
    private String buttonName;

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

    public String getRouterId() {
        return routerId;
    }

    public void setRouterId(String routerId) {
        this.routerId = routerId == null ? null : routerId.trim();
    }

    public String getButtonIcon() {
        return buttonIcon;
    }

    public void setButtonIcon(String buttonIcon) {
        this.buttonIcon = buttonIcon == null ? null : buttonIcon.trim();
    }

    public String getButtonCode() {
        return buttonCode;
    }

    public void setButtonCode(String buttonCode) {
        this.buttonCode = buttonCode == null ? null : buttonCode.trim();
    }

    public String getButtonName() {
        return buttonName;
    }

    public void setButtonName(String buttonName) {
        this.buttonName = buttonName == null ? null : buttonName.trim();
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