package com.xm.util.dingding.robot.requestParams;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDSendRobotToOneMsgParam {
    private String robotCode;
    private List<String> userIds;
    private String msgKey;
    private String msgParam;
}
