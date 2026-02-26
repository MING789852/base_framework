package com.xm.util.dingding.robot.requestRes;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DDRecallRobotToOneMsgRes {
    private List<String> successResult;
    private Map<String, String> failedResult;
}
