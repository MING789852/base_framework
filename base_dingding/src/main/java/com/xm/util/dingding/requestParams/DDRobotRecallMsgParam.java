package com.xm.util.dingding.requestParams;

import lombok.Data;

import java.util.List;

@Data
public class DDRobotRecallMsgParam {
    private String robotCode;
    private List<String> processQueryKeys;
}
