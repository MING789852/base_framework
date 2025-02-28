package com.xm.util.dingding.requestRes;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DDCreateRobotCardRes {

    private boolean success;
    private ResultDTO result;

    @NoArgsConstructor
    @Data
    public static class ResultDTO {
        private String processQueryKey;
    }
}
