package com.xm.auth.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 用户
 */
@Data
public class TcUser implements Serializable {
    /**
     * 主键
     */
    @Size(max = 32, message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
     * 账号
     */
    @Size(max = 100, message = "账号最大长度要小于 100")
    @NotBlank(message = "账号不能为空")
    private String username;

    /**
     * 密码
     */
    @Size(max = 100, message = "密码最大长度要小于 100")
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String nickName;

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
     * 工号
     */
    @Size(max = 50, message = "工号最大长度要小于 50")
    private String jobNumber;

    /**
     * 用户类型 由UserTypeEnum决定
     */
    @NotNull(message = "用户类型")
    private Integer userType;

    /**
     * 邮箱地址
     */
    @Size(max = 100, message = "邮箱地址最大长度要小于 100")
    private String email;

    /**
     * 手机号
     */
    @Size(max = 50, message = "手机号最大长度要小于 50")
    private String phoneNumber;

    /**
     * 对应管理人（领导）
     */
    @Size(max = 32, message = "对应管理人（领导）最大长度要小于 32")
    private String managerUserid;

    /**
     * 最后一次登录时间
     */
    private Date lastLoginTime;

    private static final long serialVersionUID = 1L;
}