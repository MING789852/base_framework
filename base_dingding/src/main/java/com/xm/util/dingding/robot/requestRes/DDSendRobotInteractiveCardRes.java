package com.xm.util.dingding.robot.requestRes;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DDSendRobotInteractiveCardRes {

    private boolean success;
    private ResultDTO result;

    @NoArgsConstructor
    @Data
    public static class ResultDTO {
        private String processQueryKey;
    }
}
