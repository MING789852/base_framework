package com.xm.configuration.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

import java.util.concurrent.ConcurrentHashMap;

@EnableSpringHttpSession
@Configuration
public class LocalHttpSessionConfig {

    @Bean
    public SessionRepository<?> localSessionRepository() {
        return new MapSessionRepository(new ConcurrentHashMap<>());
    }
}
