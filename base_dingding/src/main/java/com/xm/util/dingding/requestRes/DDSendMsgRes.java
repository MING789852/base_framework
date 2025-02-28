package com.xm.util.dingding.requestRes;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DDSendMsgRes {
    private int errcode;
    private long task_id;
    private String request_id;
    private String errmsg;
}
