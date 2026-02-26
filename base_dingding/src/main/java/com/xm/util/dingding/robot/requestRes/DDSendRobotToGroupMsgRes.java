package com.xm.util.dingding.robot.requestRes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DDSendRobotToGroupMsgRes {
    @JsonProperty("errcode")
    private String errcode;
    @JsonProperty("errmsg")
    private String errmsg;
}
