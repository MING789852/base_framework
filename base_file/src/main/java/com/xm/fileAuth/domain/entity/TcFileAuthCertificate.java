package com.xm.fileAuth.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class TcFileAuthCertificate implements Serializable {
    /**
    * 主键
    */
    @Size(max = 32,message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
    * 文件ID
    */
    @Size(max = 32,message = "文件ID最大长度要小于 32")
    @NotBlank(message = "文件ID不能为空")
    private String fileId;

    /**
    * 文件名称
    */
    @Size(max = 50,message = "文件名称最大长度要小于 50")
    @NotBlank(message = "文件名称不能为空")
    private String fileName;

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
    * 申请时间
    */
    private Date applyTime;

    /**
    * 授权有效日期
    */
    private Date effectiveTime;

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