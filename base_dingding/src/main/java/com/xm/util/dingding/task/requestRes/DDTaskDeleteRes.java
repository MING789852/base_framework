package com.xm.util.dingding.task.requestRes;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DDTaskDeleteRes {
    private boolean result;
    private String requestId;

    //错误返回
    private String code;
    private String message;
}
