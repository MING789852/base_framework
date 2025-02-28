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
     * 是否完成
     */
    private Boolean judgeFinish;

    /**
     * 跳转url
     */
    private JumpUrlParam jumpUrlParam;

    /**
     * 接收人员
     */
    private List<String> toUserIdList;

    /**
     * 抄送人员
     */
    private List<String> ccUserIdList;

    /**
     * 消息携带文件
     */
    private Map<String, InputStream> fileMap;


}
