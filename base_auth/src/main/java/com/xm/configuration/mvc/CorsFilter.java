package com.xm.configuration.mvc;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//过滤器的执行时机是先于拦截器的，所以利用过滤器对请求进行处理，解决跨域问题
@Configuration
@Slf4j
public class CorsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        //获取跨域请求头来源
        String origin = httpRequest.getHeader("origin");
        String method = httpRequest.getMethod();
        if (StrUtil.isBlank(origin)){
            origin="*";
        }else {
            log.info("跨域请求来源->{}; method->{}",origin,method);
        }
        // 设置跨域相关的HTTP头
        httpResponse.setHeader("Access-Control-Allow-Origin", origin); // 允许所有来源
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type,accesstoken");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

        // 对于预检请求，直接返回
        if ("OPTIONS".equalsIgnoreCase(method)) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 继续请求处理
        chain.doFilter(request, response);
    }
}

