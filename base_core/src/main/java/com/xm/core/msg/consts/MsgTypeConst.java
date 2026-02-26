package com.xm.core.msg.consts;

import java.util.Arrays;
import java.util.List;

public class MsgTypeConst {
    public static final String email = "emailMsgService";
    public static final String ddTaskTip = "ddTaskTipMsgService";
    public static final String ddPersonalTaskTip= "ddPersonalTaskTipMsgService";
    public static final String ddRobotMsg = "ddRobotMsgService";
    public static final String ddCustomizeRobotGroupMsg = "ddCustomizeRobotGroupMsgService";

    public static final List<MsgTypeItem> msgTypeItemList=Arrays.asList(
            new MsgTypeItem(email,"邮件"),
            new MsgTypeItem(ddTaskTip,"钉钉待办"),
            new MsgTypeItem(ddPersonalTaskTip,"钉钉个人待办"),
            new MsgTypeItem(ddRobotMsg,"钉钉机器人消息"),
            new MsgTypeItem(ddCustomizeRobotGroupMsg,"钉钉自定义机器人发送群消息")
    );

}
