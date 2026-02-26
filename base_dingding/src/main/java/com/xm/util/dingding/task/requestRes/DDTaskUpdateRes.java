package com.xm.util.dingding.task.requestRes;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DDTaskUpdateRes {
    private boolean result;

    //错误返回
    private String code;
    private String message;
}
