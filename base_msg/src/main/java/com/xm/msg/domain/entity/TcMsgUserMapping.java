package com.xm.msg.domain.entity;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 消息发送对应用户映射
 */
@Data
public class TcMsgUserMapping implements Serializable {
    /**
    * tc_user表主键
    */
    @Size(max = 50,message = "tc_user表主键最大长度要小于 50")
    @NotBlank(message = "tc_user表主键不能为空")
    private String userId;

    /**
    * 映射关系
    */
    @Size(max = 50,message = "映射关系最大长度要小于 50")
    @NotBlank(message = "映射关系不能为空")
    private String mapping;

    /**
    * 类型
    */
    @Size(max = 50,message = "类型最大长度要小于 50")
    @NotBlank(message = "类型不能为空")
    private String type;

    private static final long serialVersionUID = 1L;
}