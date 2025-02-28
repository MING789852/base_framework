package com.xm.advice.exception;

import com.xm.advice.exception.exception.CommonException;
import com.xm.advice.exception.exception.ErrorResultPageException;
import com.xm.core.params.JumpResultPage;
import com.xm.core.params.Result;
import com.xm.core.params.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class ExceptionAdvice {
    /**
     * 处理普通抛出异常
     */
    @ExceptionHandler({ErrorResultPageException.class})
    @ResponseBody
    public JumpResultPage handleErrorResultPageException(ErrorResultPageException e) {
        return JumpResultPage.error(e.getMsg());
    }

    /**
     * 处理普通抛出异常
     */
    @ExceptionHandler({CommonException.class})
    @ResponseBody
    public Result<Object> handleCmException(CommonException e) {
        if (e.getCode()!=null){
            return Result.error(e.getCode(), e.getMessage(),e.getData());
        } else {
            if (e.getData()!=null){
                return Result.error(ResultCode.ERROR.getCode(), e.getMessage(),e.getData());
            }else {
                return Result.error(e.getMessage());
            }
        }
    }

    /**
     * 处理字段校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result<Map<String,String>> handleValidException(MethodArgumentNotValidException e){
        log.error("校验异常",e);
        Map<String,String> collect=e.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField,FieldError::getDefaultMessage));
        return Result.errorForData("请求参数校验异常",collect);
    }


    @ExceptionHandler({Exception.class})
    @ResponseBody
    public Result<String> handleException(Exception e){
        log.error("异常",e);
        return Result.error(500,e.getMessage());
    }
}
