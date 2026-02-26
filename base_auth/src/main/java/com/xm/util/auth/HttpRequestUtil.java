package com.xm.util.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
public class HttpRequestUtil {

    /**
     * 获取当前HttpSessionId
     */
    public static String getCurrentHttpSessionId(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes==null){
            return null;
        }
        return requestAttributes.getSessionId();
    }

    /**
     * 获取当前HttpServletRequest
     */
    public static HttpServletRequest getCurrentHttpServletRequest(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes==null){
            return null;
        }
        return requestAttributes.getRequest();
    }


    /**
     * 获取当前session(不存在则返回null)
     */
    public static HttpSession getCurrentHttpSession(){
        HttpServletRequest currentHttpServletRequest = getCurrentHttpServletRequest();
        if (currentHttpServletRequest==null){
            return null;
        }
        //允许创建session设置为false
        return currentHttpServletRequest.getSession(false);
    }


    /**
     * 获取当前session(不存在则创建)
     */
    public static HttpSession getCurrentHttpSessionWithCreate(){

        HttpServletRequest currentHttpServletRequest = getCurrentHttpServletRequest();
        if (currentHttpServletRequest==null){
            return null;
        }
        return currentHttpServletRequest.getSession(true);
    }
}
