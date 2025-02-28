package com.xm.configuration.interceptor;


import com.xm.advice.exception.exception.CommonException;
import com.xm.annotation.IgnoreAuth;
import com.xm.util.systemFront.SystemFrontTokenUtil;
import com.xm.util.systemFront.params.VerifyTokenResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Slf4j
@Order(2)
public class SyncFrontInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是映射到方法直接拒绝
        if(!(handler instanceof HandlerMethod)){
            log.error("执行同步url拦截,url->{},失败->非映射方法", request.getRequestURI());
            return false;
        }
        // 判断是否直接放行
        HandlerMethod handlerMethod=(HandlerMethod)handler;
        Method method=handlerMethod.getMethod();
        if (method.isAnnotationPresent(IgnoreAuth.class)) {
            log.info("执行同步url拦截,url->{},成功->IgnoreAuth", request.getRequestURI());
            return true;
        }

        String headerAccessToken = request.getHeader("accessToken");
        VerifyTokenResult verifyTokenResult = SystemFrontTokenUtil.verifySystemFrontAccessToken(headerAccessToken);
        if (verifyTokenResult.isSuccess()){
            log.info("执行同步url拦截,url->{},成功->鉴权成功", request.getRequestURI());
            return true;
        }else {
            log.error("执行同步url拦截,url->{},成功->鉴权失败", request.getRequestURI());
            throw new CommonException(verifyTokenResult.getErrorMsg());
        }
    }
}
