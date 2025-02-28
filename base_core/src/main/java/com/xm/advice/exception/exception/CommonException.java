package com.xm.advice.exception.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommonException extends RuntimeException {
    private String msg;
    private Integer code;
    private Object data;

    public CommonException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public CommonException(Integer code, String msg) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }


    public CommonException(Integer code, String msg, Object data) {
        super(msg);
        this.msg = msg;
        this.code = code;
        this.data = data;
    }
}
