package com.xm.util.dingding.task.commonParams;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class NotifyConfigsDTO {
    //是否发送钉钉弹框通知：
    //0：不发送
    //1：发送
    private String dingNotify;
    //是否发送系统APN通知：
    //true：发送
    //false：不发送
    //当未设置时取dingNotify的设置值
    private String sendTodoApn;
    //是否发送待办助手通知：
    //true：发送，默认值
    //false：不发送
    private String sendAssistantChat;
}
