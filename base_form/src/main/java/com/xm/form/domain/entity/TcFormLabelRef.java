package com.xm.form.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 标签关联
 */
public class TcFormLabelRef implements Serializable {
    /**
    * 关联业务id
    */
    @Size(max = 32,message = "关联业务id最大长度要小于 32")
    @NotBlank(message = "关联业务id不能为空")
    private String refBusinessId;

    /**
    * 关联类型
    */
    @Size(max = 50,message = "关联类型最大长度要小于 50")
    @NotBlank(message = "关联类型不能为空")
    private String refType;

    /**
    * 关联标签id
    */
    @Size(max = 32,message = "关联标签id最大长度要小于 32")
    @NotBlank(message = "关联标签id不能为空")
    private String refLabelId;

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

    public String getRefBusinessId() {
        return refBusinessId;
    }

    public void setRefBusinessId(String refBusinessId) {
        this.refBusinessId = refBusinessId == null ? null : refBusinessId.trim();
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType == null ? null : refType.trim();
    }

    public String getRefLabelId() {
        return refLabelId;
    }

    public void setRefLabelId(String refLabelId) {
        this.refLabelId = refLabelId == null ? null : refLabelId.trim();
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
        sb.append(", refBusinessId=").append(refBusinessId);
        sb.append(", refType=").append(refType);
        sb.append(", refLabelId=").append(refLabelId);
        sb.append(", createDate=").append(createDate);
        sb.append(", createUser=").append(createUser);
        sb.append(", updateDate=").append(updateDate);
        sb.append(", updateUser=").append(updateUser);
        sb.append("]");
        return sb.toString();
    }
}