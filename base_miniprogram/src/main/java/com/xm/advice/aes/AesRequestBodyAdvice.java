package com.xm.advice.aes;

import cn.hutool.core.util.StrUtil;
import com.xm.advice.aes.annotation.AesDecrypt;
import com.xm.advice.exception.exception.CommonException;

import com.xm.util.miniprogram.MiniProgramUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;

@Slf4j
@RestControllerAdvice(basePackages={"com.tc.module"})
public class AesRequestBodyAdvice extends RequestBodyAdviceAdapter {
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasMethodAnnotation(AesDecrypt.class);
    }


    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        log.info("执行AES解密");
        try {
            if (body instanceof String) {
                return MiniProgramUtil.decryptParam(body.toString());
            } else {
                return body;
            }
        } catch (Exception e) {
            String msg= StrUtil.format("\n 类 {} \n 方法 {} \n 参数 {} 解密失败"
                    ,parameter.getExecutable().getDeclaringClass().getName(),parameter.getMethod().getName(),body.toString());
            log.error(msg,e);
            throw new CommonException("请求参数错误");
        }
    }

}
