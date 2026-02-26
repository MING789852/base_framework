package com.xm.configuration.session.cookie;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.server.Cookie;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@ConditionalOnClass(CookieHttpSessionIdResolver.class)
public class CookieHttpSessionConfig {
    private final ServerProperties serverProperties;
    @Bean
    public CookieHttpSessionIdResolver httpSessionIdResolver() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        //修改cookie name以保证spring sessionId和cookie sessionId一致
        String cookieName = Optional.ofNullable(serverProperties)
                .map(ServerProperties::getServlet)
                .map(ServerProperties.Servlet::getSession)
                .map(Session::getCookie)
                .map(Cookie::getName).orElse("SESSION");
        serializer.setCookieName(cookieName); // 设置cookie名称
        CookieHttpSessionIdResolver cookieHttpSessionIdResolver = new CookieHttpSessionIdResolver();
        cookieHttpSessionIdResolver.setCookieSerializer(serializer);
        return cookieHttpSessionIdResolver;
    }
}
