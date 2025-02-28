package com.xm.util.dingding.requestRes;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DDRobotCardCallbackRegisterRes {
    private int errcode;
    private String errmsg;
    private String result;
    private boolean success;
    private String request_id;
}
