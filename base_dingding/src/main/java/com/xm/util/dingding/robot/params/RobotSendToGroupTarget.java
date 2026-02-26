package com.xm.util.dingding.robot.params;

import com.xm.core.msg.params.ButtonParam;
import lombok.Data;

import java.util.List;

@Data
public class RobotSendToGroupTarget {
    private String groupAccessToken;
    private String isAtAll;
    private List<String> userIdList;
    private List<ButtonParam> buttonParamList;
}
