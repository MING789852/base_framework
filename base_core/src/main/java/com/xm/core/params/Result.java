package com.xm.core.params;

import lombok.Data;

import java.io.Serializable;

/**
 * 封装返回值
 */
@Data
public class Result<T> implements Serializable{
    private Integer code;
    private String msg;
    private T data;

    public Result(Integer code,T data,String msg){
        this.code=code;
        this.data=data;
        this.msg=msg;
    }

    public static<T> Result<T> success(String msg){
        return new Result<>(ResultCode.SUCCESS.getCode(),null,msg);
    }

    public static<T> Result<T> successForData(T data){
        return new Result<>(ResultCode.SUCCESS.getCode(),data,ResultCode.SUCCESS.getDes());
    }

    public static<T> Result<T> error(ResultCode code,String msg){
        return new Result<>(code.getCode(),null,msg);
    }

    public static<T> Result<T> error(ResultCode code){
        return new Result<>(code.getCode(),null,code.getDes());
    }

    public static<T> Result<T> error(Integer code,String msg){
        return new Result<>(code,null,msg);
    }

    public static<T> Result<T> error(Integer code,String msg,T data){
        return new Result<>(code,data,msg);
    }

    public static<T> Result<T> error(String msg){
        return new Result<>(ResultCode.ERROR.getCode(),null,msg);
    }

    public static<T> Result<T> errorForData(String msg,T data){
        return new Result<>(ResultCode.ERRORDATA.getCode(),data,msg);
    }
}
