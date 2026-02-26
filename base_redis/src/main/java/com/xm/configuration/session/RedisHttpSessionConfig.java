package com.xm.configuration.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@Configuration
@Slf4j
@RequiredArgsConstructor
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 7200)
@ConditionalOnProperty(name = "spring.session.store-type", havingValue = "REDIS")
public class RedisHttpSessionConfig {

}
