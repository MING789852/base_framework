package com.xm.status.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 状态明细模型
 */
public class TcStatusDetailModel implements Serializable {
    /**
    * 主键
    */
    @Size(max = 32,message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
    * 关联tc_status_main_model
    */
    @Size(max = 32,message = "关联tc_status_main_model最大长度要小于 32")
    @NotBlank(message = "关联tc_status_main_model不能为空")
    private String statusMainModelId;

    /**
    * 关联tc_form_main_model
    */
    @Size(max = 30,message = "关联tc_form_main_model最大长度要小于 30")
    private String formMainModelId;

    /**
    * 序号
    */
    private Integer sequence;

    /**
    * 编码
    */
    @Size(max = 100,message = "编码最大长度要小于 100")
    @NotBlank(message = "编码不能为空")
    private String code;

    /**
    * 名称
    */
    @Size(max = 100,message = "名称最大长度要小于 100")
    @NotBlank(message = "名称不能为空")
    private String name;

    /**
    * 备用字段1
    */
    @Size(max = 100,message = "备用字段1最大长度要小于 100")
    private String other1;

    /**
    * 备用字段2
    */
    @Size(max = 100,message = "备用字段2最大长度要小于 100")
    private String other2;

    /**
    * 备用字段3
    */
    @Size(max = 100,message = "备用字段3最大长度要小于 100")
    private String other3;

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

    public String getStatusMainModelId() {
        return statusMainModelId;
    }

    public void setStatusMainModelId(String statusMainModelId) {
        this.statusMainModelId = statusMainModelId == null ? null : statusMainModelId.trim();
    }

    public String getFormMainModelId() {
        return formMainModelId;
    }

    public void setFormMainModelId(String formMainModelId) {
        this.formMainModelId = formMainModelId == null ? null : formMainModelId.trim();
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getOther1() {
        return other1;
    }

    public void setOther1(String other1) {
        this.other1 = other1 == null ? null : other1.trim();
    }

    public String getOther2() {
        return other2;
    }

    public void setOther2(String other2) {
        this.other2 = other2 == null ? null : other2.trim();
    }

    public String getOther3() {
        return other3;
    }

    public void setOther3(String other3) {
        this.other3 = other3 == null ? null : other3.trim();
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
        sb.append(", statusMainModelId=").append(statusMainModelId);
        sb.append(", formMainModelId=").append(formMainModelId);
        sb.append(", sequence=").append(sequence);
        sb.append(", code=").append(code);
        sb.append(", name=").append(name);
        sb.append(", other1=").append(other1);
        sb.append(", other2=").append(other2);
        sb.append(", other3=").append(other3);
        sb.append(", createDate=").append(createDate);
        sb.append(", createUser=").append(createUser);
        sb.append(", updateDate=").append(updateDate);
        sb.append(", updateUser=").append(updateUser);
        sb.append("]");
        return sb.toString();
    }
}