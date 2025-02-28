package com.xm.util.auth;

import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.util.bean.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.session.StoreType;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class StorageSessionUtil {

    private final static SessionRepository<?> sessionRepository;

    static {
        SessionProperties sessionProperties = SpringBeanUtil.getBeanByClass(SessionProperties.class);
        if (StoreType.NONE.equals(sessionProperties.getStoreType())){
            sessionRepository = SpringBeanUtil.getBeanByClass(MapSessionRepository.class);
        }else {
            sessionRepository = SpringBeanUtil.getBeanByClass(RedisIndexedSessionRepository.class);
        }
    }

//    public static SessionRepository<Session> getSessionRepository(){
//        HttpServletRequest request = HttpRequestUtil.getCurrentHttpServletRequest();
//        if (request==null){
//            return null;
//        }
//        SessionRepository<Session> sessionRepository = (SessionRepository)request.getAttribute(SessionRepositoryFilter.SESSION_REPOSITORY_ATTR);
//        if (sessionRepository==null){
//            throw new CommonException("sessionRepository不存在");
//        }
//        return sessionRepository;
//    }

    public static Session findSessionBySessionId(String sessionId){
        if (StrUtil.isBlank(sessionId)){
            return null;
        }
        return sessionRepository.findById(sessionId);
    }

    public static void deleteSessionBySessionId(String sessionId){
        sessionRepository.deleteById(sessionId);
    }
}
