package com.xm.form.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 表单主表实例
 */
public class TcFormMainInstance implements Serializable {
    /**
    * 主键
    */
    @Size(max = 32,message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
    * 关联tc_form_main_model
    */
    @Size(max = 32,message = "关联tc_form_main_model最大长度要小于 32")
    @NotBlank(message = "关联tc_form_main_model不能为空")
    private String formMainModelId;

    /**
    * 关联id
    */
    @Size(max = 32,message = "关联id最大长度要小于 32")
    @NotBlank(message = "关联id不能为空")
    private String refId;

    /**
    * 关联类型
    */
    @Size(max = 50,message = "关联类型最大长度要小于 50")
    @NotBlank(message = "关联类型不能为空")
    private String refType;

    /**
    * 关联备用字段
    */
    @Size(max = 50,message = "关联备用字段最大长度要小于 50")
    @NotBlank(message = "关联备用字段不能为空")
    private String refFiled1;

    /**
    * 关联备用字段
    */
    @Size(max = 50,message = "关联备用字段最大长度要小于 50")
    @NotBlank(message = "关联备用字段不能为空")
    private String refFiled2;

    /**
    * 实例名称
    */
    @Size(max = 50,message = "实例名称最大长度要小于 50")
    @NotBlank(message = "实例名称不能为空")
    private String name;

    /**
    * 实例编码
    */
    @Size(max = 50,message = "实例编码最大长度要小于 50")
    @NotBlank(message = "实例编码不能为空")
    private String code;

    /**
    * 实例是否结束
    */
    @NotNull(message = "实例是否结束不能为null")
    private Boolean finish;

    /**
    * 创建日期
    */
    private Date createDate;

    /**
    * 创建人员
    */
    @Size(max = 50,message = "创建人员最大长度要小于 50")
    private String createUser;

    /**
    * 更新日期
    */
    private Date updateDate;

    /**
    * 更新人员
    */
    @Size(max = 50,message = "更新人员最大长度要小于 50")
    private String updateUser;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getFormMainModelId() {
        return formMainModelId;
    }

    public void setFormMainModelId(String formMainModelId) {
        this.formMainModelId = formMainModelId == null ? null : formMainModelId.trim();
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId == null ? null : refId.trim();
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType == null ? null : refType.trim();
    }

    public String getRefFiled1() {
        return refFiled1;
    }

    public void setRefFiled1(String refFiled1) {
        this.refFiled1 = refFiled1 == null ? null : refFiled1.trim();
    }

    public String getRefFiled2() {
        return refFiled2;
    }

    public void setRefFiled2(String refFiled2) {
        this.refFiled2 = refFiled2 == null ? null : refFiled2.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public Boolean getFinish() {
        return finish;
    }

    public void setFinish(Boolean finish) {
        this.finish = finish;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", formMainModelId=").append(formMainModelId);
        sb.append(", refId=").append(refId);
        sb.append(", refType=").append(refType);
        sb.append(", refFiled1=").append(refFiled1);
        sb.append(", refFiled2=").append(refFiled2);
        sb.append(", name=").append(name);
        sb.append(", code=").append(code);
        sb.append(", finish=").append(finish);
        sb.append(", createDate=").append(createDate);
        sb.append(", createUser=").append(createUser);
        sb.append(", updateDate=").append(updateDate);
        sb.append(", updateUser=").append(updateUser);
        sb.append("]");
        return sb.toString();
    }
}