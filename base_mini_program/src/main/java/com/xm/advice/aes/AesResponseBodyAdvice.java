package com.xm.advice.aes;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.aes.annotation.AesEncrypt;
import com.xm.util.miniprogram.MiniProgramUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice(basePackages={"com.tc.module"})
public class AesResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return returnType.hasMethodAnnotation(AesEncrypt.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        log.info("执行AES加密");
        try {
            if (body==null){
                return body;
            } else {
                String temp;
                if (!(body instanceof String)){
                    temp= JSONUtil.toJsonStr(body);
                }else {
                    temp=body.toString();
                }
                return MiniProgramUtil.encryptParam(temp);
            }
        } catch (Exception e) {
            String msg=StrUtil.format("\n 类 {} \n 方法 {} \n 返回值 {} 加密失败"
                    ,returnType.getExecutable().getDeclaringClass().getName(),returnType.getMethod().getName(),body.toString());
            log.error(msg,e);
            return body;
        }
    }
}
