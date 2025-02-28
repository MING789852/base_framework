package com.xm.configuration.mvc;


import com.xm.configuration.interceptor.SyncFrontInterceptor;
import com.xm.configuration.interceptor.SystemAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class SystemWebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(new SystemAuthInterceptor()).excludePathPatterns("/**/syncFront/**");
        registry
                .addInterceptor(new SyncFrontInterceptor()).addPathPatterns("/**/syncFront/**");
    }
}
