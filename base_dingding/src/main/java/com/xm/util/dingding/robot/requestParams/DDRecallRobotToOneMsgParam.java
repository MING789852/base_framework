package com.xm.util.dingding.robot.requestParams;

import lombok.Data;

import java.util.List;

@Data
public class DDRecallRobotToOneMsgParam {
    private String robotCode;
    private List<String> processQueryKeys;
}
