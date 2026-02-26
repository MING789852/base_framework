package com.xm.core.msg.params;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class Msg {
    /**
     * 消息ID
     */
    private String id;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务编码
     */
    private String businessKey;


    /**
     * 描述
     */
    private String title;

    /**
     * 内容
     */
    private String content;


    /**
     * 额外信息
     */
    private String info;


    /**
     * 群组id
     */
    private String groupId;

    /**
     * "1" 为@所有人
     * "0" 为@指定人员
     * 带url类型的消息也无法生效
     * 为空则不生效
     * 群组@所有人
     */
    private Boolean groupAtAll;


    /**
     * 是否完成
     */
    private boolean judgeFinish;

    /**
     * 跳转完成后消息是否结束
     */
    private Boolean jumpAfterFinish;

    /**
     * 跳转url
     */
    private JumpUrlParam jumpUrlParam;

    /**
     * 按钮参数
     */
    private List<ButtonParam> buttonParamList;

    /**
     * 接收人员
     */
    private List<String> toUserIdList;


    /**
     * 创建人员ID
     */
    private String creatorId;

    /**
     * 抄送人员
     */
    private List<String> ccUserIdList;

    /**
     * 消息携带文件
     */
    private Map<String, byte[]> fileMap;

    /**
     * 其他信息
     */
    private Map<String, Object> otherMap;


    /**
     * 消息发送异常原因
     */
    private ErrorSendResult errorSendResult;
}
