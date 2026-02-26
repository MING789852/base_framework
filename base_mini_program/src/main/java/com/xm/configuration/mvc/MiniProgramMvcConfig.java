package com.xm.configuration.mvc;

import com.xm.configuration.filter.DecodeMiniTokenFilter;
import com.xm.configuration.interceptor.MiniAuthInterceptor;
import com.xm.configuration.resolver.OpenIdArgumentResolver;
import com.xm.configuration.wechat.miniprogram.MiniprogramConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnClass(MiniprogramConfig.class)
public class MiniProgramMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(new MiniAuthInterceptor()).addPathPatterns("/**/mini/**");
    }

    @Bean
    @ConditionalOnClass(DecodeMiniTokenFilter.class)
    public FilterRegistrationBean<DecodeMiniTokenFilter> registrationDecodeMiniTokenFilter(){
        FilterRegistrationBean<DecodeMiniTokenFilter> filterRegistrationBean=new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new DecodeMiniTokenFilter());
        List<String> urlList = new ArrayList<>();
        urlList.add("/mini/*");
        filterRegistrationBean.setUrlPatterns(urlList);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.setName("DecodeMiniTokenFilter");
        return filterRegistrationBean;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new OpenIdArgumentResolver());
    }
}
