package com.xm.util.auth;

import cn.hutool.core.util.StrUtil;
import com.xm.util.bean.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.session.StoreType;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

@Slf4j
public class StorageSessionUtil {

    private final static SessionRepository<?> sessionRepository;

    static {
        SessionProperties sessionProperties = SpringBeanUtil.getBeanByClass(SessionProperties.class);
        if (StoreType.NONE.equals(sessionProperties.getStoreType())){
            sessionRepository = SpringBeanUtil.getBeanByClass(MapSessionRepository.class);
            log.info("执行本地 session存储，sessionRepository=>{}", sessionRepository);
        }else {
            sessionRepository = SpringBeanUtil.getBeanByClass(RedisIndexedSessionRepository.class);
            log.info("执行redis session存储，sessionRepository=>{}", sessionRepository);
        }
    }

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
