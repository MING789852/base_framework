package com.xm.flowable.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 正在审批业务
 */
public class TcBusinessApproving implements Serializable {
    /**
    * 主键
    */
    @Size(max = 32,message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
    * 业务id
    */
    @Size(max = 32,message = "业务id最大长度要小于 32")
    @NotBlank(message = "业务id不能为空")
    private String businessId;

    /**
    * 业务类型
    */
    @Size(max = 50,message = "业务类型最大长度要小于 50")
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    /**
    * 关联tc_user
    */
    @Size(max = 32,message = "关联tc_user最大长度要小于 32")
    @NotBlank(message = "关联tc_user不能为空")
    private String userId;

    /**
    * 关联tc_user
    */
    @Size(max = 32,message = "关联tc_user最大长度要小于 32")
    @NotBlank(message = "关联tc_user不能为空")
    private String userName;

    /**
    * 创建日期
    */
    private Date createDate;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId == null ? null : businessId.trim();
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType == null ? null : businessType.trim();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}