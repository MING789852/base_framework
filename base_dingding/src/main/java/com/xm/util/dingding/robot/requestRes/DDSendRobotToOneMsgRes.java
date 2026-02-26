package com.xm.util.dingding.robot.requestRes;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDSendRobotToOneMsgRes {
    private boolean result;

    private String processQueryKey;
    private List<String> invalidStaffIdList;
    private List<String> flowControlledStaffIdList;

    //错误返回
    private String code;
    private String message;
}
