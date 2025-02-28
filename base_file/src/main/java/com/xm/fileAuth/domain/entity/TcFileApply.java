package com.xm.fileAuth.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class TcFileApply implements Serializable {
    /**
    * 主键
    */
    @Size(max = 32,message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
    * 关联ID
    */
    @Size(max = 50,message = "关联ID最大长度要小于 50")
    private String refId;

    /**
    * 关联类型
    */
    @Size(max = 50,message = "关联类型最大长度要小于 50")
    private String refType;

    /**
    * 申请人
    */
    @Size(max = 32,message = "申请人最大长度要小于 32")
    @NotBlank(message = "申请人不能为空")
    private String userId;

    /**
    * 申请人名称
    */
    @Size(max = 50,message = "申请人名称最大长度要小于 50")
    @NotBlank(message = "申请人名称不能为空")
    private String userName;

    /**
    * 申请备注
    */
    @Size(max = 255,message = "申请备注最大长度要小于 255")
    private String applyRemark;

    /**
    * 申请时间
    */
    private Date applyTime;

    /**
    * 申请有效日期
    */
    private Date effectiveTime;

    /**
    * 0、审批中 1、同意 2、拒绝
    */
    private Integer status;

    /**
    * 授权人id
    */
    @Size(max = 32,message = "授权人id最大长度要小于 32")
    private String operateUserId;

    /**
    * 授权人名称
    */
    @Size(max = 32,message = "授权人名称最大长度要小于 32")
    private String operateUser;

    /**
    * 授权人操作时间
    */
    private Date operateTime;

    private static final long serialVersionUID = 1L;
}