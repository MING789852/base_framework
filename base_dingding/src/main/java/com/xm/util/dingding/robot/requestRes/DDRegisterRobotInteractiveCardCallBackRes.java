package com.xm.util.dingding.robot.requestRes;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DDRegisterRobotInteractiveCardCallBackRes {
    private int errcode;
    private String errmsg;
    private String result;
    private boolean success;
    private String request_id;
}
