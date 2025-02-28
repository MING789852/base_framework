package com.xm.configuration.interceptor;

import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.UnAuthException;
import com.xm.annotation.IgnoreAuth;
import com.xm.configuration.wechat.miniprogram.MiniprogramConfig;
import com.xm.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Slf4j
@Order(2)
public class MiniAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("执行小程序授权拦截,url->{}", request.getRequestURI());
        // 如果不是映射到方法直接通过
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        // 判断是否直接放行
        HandlerMethod handlerMethod=(HandlerMethod)handler;
        Method method=handlerMethod.getMethod();
        if (method.isAnnotationPresent(IgnoreAuth.class)) {
            return true;
        }

        Object openIdObj=request.getAttribute(MiniprogramConfig.getOpenIdKey());
        if (openIdObj==null){
            log.error("解析openId为null");
            throw new UnAuthException("未授权");
        } else {
            String openId=openIdObj.toString();
            if (StrUtil.isBlank(openId)){
                log.error("解析openId为空");
                throw new UnAuthException("未授权");
            }else {
                if (Boolean.FALSE.equals(CacheUtil.hasKey(openId))){
                    log.error("redis存储openId为空");
                    throw new UnAuthException("未授权");
                }
            }
        }

        return true;
    }
}
