package com.xm.util.systemFront.params;

import lombok.Data;

@Data
public class VerifyTokenResult {
    private boolean success;
    private String errorMsg;
}
