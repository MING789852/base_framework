package com.xm.configuration.resolver;

import com.xm.configuration.resolver.annotation.OpenId;
import com.xm.configuration.wechat.miniprogram.MiniprogramConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
public class OpenIdArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(OpenId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        log.info("执行openId参数解析器,参数名称->{}",parameter.getParameterName());
        Object openId=webRequest.getAttribute(MiniprogramConfig.getOpenIdKey(),RequestAttributes.SCOPE_REQUEST);
        return openId==null?"":openId.toString();
    }
}
