package com.xm.configuration.interceptor;

import cn.hutool.core.date.DateUtil;
import com.xm.advice.exception.exception.UnAuthException;
import com.xm.annotation.IgnoreAuth;
import com.xm.auth.domain.entity.TcUser;
import com.xm.util.auth.LoginSessionUtil;
import com.xm.util.auth.TokenSessionUtil;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.auth.params.RequestAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Date;

@Slf4j
public class SystemAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果不是映射到方法直接拒绝
        if(!(handler instanceof HandlerMethod)){
            return false;
        }
        // 判断是否直接放行
        HandlerMethod handlerMethod=(HandlerMethod)handler;
        Method method=handlerMethod.getMethod();
        if (method.isAnnotationPresent(IgnoreAuth.class)) {
            return true;
        }
        //先添加访问记录
        String requestURI = request.getRequestURI();
        // 判断是否已登录
        TcUser currentLoginUserBySession = LoginSessionUtil.getCurrentLoginUserBySession();
        TcUser currentLoginUserByAccessToken = TokenSessionUtil.getCurrentLoginUserByAccessToken();

        if (currentLoginUserBySession==null&&currentLoginUserByAccessToken!=null){
            log.info("【执行系统授权拦截】【accessToken登录】url->{},用户->{}", requestURI,currentLoginUserByAccessToken.getNickName());
            UserInfoUtil.addRequestAction(new RequestAction(requestURI,DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss")),currentLoginUserByAccessToken);
        }
        if (currentLoginUserBySession!=null&&currentLoginUserByAccessToken==null){
            log.info("【执行系统授权拦截】【session登录】url->{},用户->{}", requestURI,currentLoginUserBySession.getNickName());
            UserInfoUtil.addRequestAction(new RequestAction(requestURI,DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss")),currentLoginUserBySession);
        }
        if (currentLoginUserBySession==null&&currentLoginUserByAccessToken==null){
            log.info("【执行系统授权拦截】url->{},未登录", requestURI);
            throw new UnAuthException("未登录");
        }
        return true;
    }
}
