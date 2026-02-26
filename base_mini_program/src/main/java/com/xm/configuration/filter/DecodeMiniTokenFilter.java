package com.xm.configuration.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTPayload;
import com.xm.configuration.wechat.miniprogram.MiniprogramConfig;
import com.xm.util.auth.TokenGenerateUtil;
import com.xm.util.auth.enums.TokenKeyEnum;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class DecodeMiniTokenFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String miniAccessToken=httpServletRequest.getHeader(MiniprogramConfig.getHeaderKey());
        String openId="";
        if (StrUtil.isNotBlank(miniAccessToken)){
            try {
                if (TokenGenerateUtil.verify(miniAccessToken)){
                    JWTPayload jwtPayload = TokenGenerateUtil.getPayloadByToken(miniAccessToken);
                    assert jwtPayload != null;
                    openId=jwtPayload.getClaim(TokenKeyEnum.openId.name()).toString();
                }
            }catch (Exception e){
                log.error("解析openId失败",e);
                openId = "";
            }
        }
        servletRequest.setAttribute(MiniprogramConfig.getOpenIdKey(),openId);
        //放行
        filterChain.doFilter(httpServletRequest,servletResponse);
    }
}
