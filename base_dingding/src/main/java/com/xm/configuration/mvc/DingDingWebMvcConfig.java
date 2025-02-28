package com.xm.configuration.mvc;

import com.xm.configuration.dingding.DingdingConfig;
import com.xm.configuration.interceptor.DingDingHandlerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnClass(DingdingConfig.class)
public class DingDingWebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(new DingDingHandlerInterceptor()).addPathPatterns("/**");
    }
}
