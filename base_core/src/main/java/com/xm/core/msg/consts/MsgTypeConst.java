package com.xm.core.msg.consts;

import java.util.Arrays;
import java.util.List;

public class MsgTypeConst {
    public static final String email = "emailMsgService";
    public static final String ddTaskTip = "ddTaskTipMsgService";
    public static final String ddRobot = "ddRobotMsgService";

    public static final List<MsgTypeItem> msgTypeItemList=Arrays.asList(
            new MsgTypeItem(email,"邮件"),
            new MsgTypeItem(ddTaskTip,"钉钉待办"),
            new MsgTypeItem(ddRobot,"钉钉机器人")
    );

}
