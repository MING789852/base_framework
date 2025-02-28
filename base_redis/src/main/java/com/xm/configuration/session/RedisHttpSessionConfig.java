package com.xm.configuration.session;

import com.xm.util.redis.RedisTemplateUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

@EnableSpringHttpSession
@Configuration
public class RedisHttpSessionConfig {


    @Bean
    @ConditionalOnProperty(name = "spring.session.store-type", havingValue = "REDIS")
    public SessionRepository<?> redisSessionRepository() {
        return new RedisIndexedSessionRepository(RedisTemplateUtil.getRedisTemplate());
    }
}
