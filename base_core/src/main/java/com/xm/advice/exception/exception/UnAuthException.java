package com.xm.advice.exception.exception;

public class UnAuthException extends CommonException{
    public UnAuthException(String msg) {
        super(401, msg);
    }
}
