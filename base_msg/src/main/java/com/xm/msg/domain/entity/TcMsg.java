package com.xm.msg.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 钉钉消息表
 */
@Data
public class TcMsg implements Serializable {
    /**
     * 主键
     */
    @Size(max = 50, message = "主键最大长度要小于 50")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
     * 业务类型
     */
    @Size(max = 50, message = "业务类型最大长度要小于 50")
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    /**
     * 业务编码
     */
    @Size(max = 50, message = "业务编码最大长度要小于 50")
    @NotBlank(message = "业务编码不能为空")
    private String businessKey;


    @Size(max = 32, message = "消息类型最大长度要小于 32")
    @NotBlank(message = "消息类型不能为空")
    private String type;

    /**
     * 额外信息
     */
    private String info;

    /**
     * 描述
     */
    @Size(max = 255, message = "描述最大长度要小于 255")
    private String des;

    @NotNull(message = "不能为null")
    private Boolean judgeFinish;

    /**
     * 用户id
     */
    @Size(max = 50, message = "用户id最大长度要小于 50")
    @NotBlank(message = "用户id不能为空")
    private String userId;

    /**
     * 创建日期
     */
    private Date createDate;

    /**
     * 更新日期
     */
    private Date updateDate;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}