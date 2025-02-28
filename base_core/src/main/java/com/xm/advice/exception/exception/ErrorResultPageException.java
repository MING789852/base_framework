package com.xm.advice.exception.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ErrorResultPageException extends RuntimeException{
    private String msg;

    public ErrorResultPageException(String msg) {
        super(msg);
        this.msg = msg;
    }

}
