package com.xm.util.dingding.requestRes;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DDRobotRecallMsgRes {
    private List<String> successResult;
    private Map<String, String> failedResult;
}
