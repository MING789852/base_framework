package com.xm.util.dingding.requestParams;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDRobotSendMsgParam {
    private String robotCode;
    private List<String> userIds;
    private String msgKey;
    private String msgParam;
}
