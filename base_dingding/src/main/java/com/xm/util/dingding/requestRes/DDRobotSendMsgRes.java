package com.xm.util.dingding.requestRes;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDRobotSendMsgRes {
    private String processQueryKey;
    private List<String> invalidStaffIdList;
    private List<String> flowControlledStaffIdList;
}
