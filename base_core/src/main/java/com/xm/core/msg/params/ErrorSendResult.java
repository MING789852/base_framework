package com.xm.core.msg.params;

import lombok.Data;

@Data
public class ErrorSendResult {
    private String message;
    private String code;
    private Exception exception;

    public ErrorSendResult(String code, String message, Exception exception) {
        this.code = code;
        this.message = message;
        this.exception = exception;
    }
}
