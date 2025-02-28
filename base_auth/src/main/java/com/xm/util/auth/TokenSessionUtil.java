package com.xm.util.auth;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTPayload;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcUser;
import com.xm.consts.AuthCacheKey;
import com.xm.consts.RequestHeader;
import com.xm.consts.SessionAttr;
import com.xm.util.auth.enums.TokenKeyEnum;
import com.xm.util.auth.params.SessionMappingData;
import com.xm.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.Session;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TokenSessionUtil {

    /**
     * 根据accessToken获取当前登录用户的角色列表
     */
    public static List<TcRole> getCurrentLoginRoleByAccessToken(){
        //允许创建session设置为false
        Session sessionByAccessToken = TokenSessionUtil.getCurrentSessionByAccessToken();
        Object roleObjList;
        if (sessionByAccessToken==null){
            return new ArrayList<>();
        }else {
            roleObjList = sessionByAccessToken.getAttribute(SessionAttr.attr_roleKey);
        }
        if (roleObjList==null){
            return new ArrayList<>();
        }
        if (roleObjList instanceof List){
            List<?> list= (List<?>) roleObjList;
            return BeanUtil.copyToList(list,TcRole.class);
        }else {
            return new ArrayList<>();
        }
    }

    /**
     * 根据accessToken获取session
     */
    public static Session getCurrentSessionByAccessToken(){
        String userName = getUserNameByAccessToken();
        if (StrUtil.isBlank(userName)){
            return null;
        }
        return getSessionByUserName(userName);
    }

    /**
     * 根据accessToken获取用户
     */
    public static TcUser getCurrentLoginUserByAccessToken(){
        String userName = getUserNameByAccessToken();
        if (StrUtil.isBlank(userName)){
            return null;
        }
        return getCurrentLoginUserByUserName(userName);
    }

    /**
     * 根据用户名查询已登录用户
     */
    private static TcUser getCurrentLoginUserByUserName(String username){
        Session session=getSessionByUserName(username);
        if (session!=null){
            return session.getAttribute(SessionAttr.attr_userKey);
        }
        return null;
    }

    /**
     * 根据用户名查询session
     */
    private static Session getSessionByUserName(String username){
        SessionMappingData sessionMappingData= (SessionMappingData) CacheUtil.get(AuthCacheKey.getUserAndSession(username));
        if (sessionMappingData==null){
            return null;
        }
        String sessionId = sessionMappingData.getSessionId();
        if (StrUtil.isNotBlank(sessionId)) {
            return StorageSessionUtil.findSessionBySessionId(sessionId);
        }
        return null;
    }


    /**
     * 根据accessToken获取用户名
     */
    private static String getUserNameByAccessToken(){
        HttpServletRequest currentHttpServletRequest = HttpRequestUtil.getCurrentHttpServletRequest();
        if (currentHttpServletRequest==null){
            return null;
        }
        String accessToken = currentHttpServletRequest.getParameter(RequestHeader.accessTokenKey);
        if (StrUtil.isBlank(accessToken)){
            return null;
        }
        JWTPayload jwtPayload= TokenGenerateUtil.getPayloadByToken(accessToken);
        if (jwtPayload==null){
            log.error("【AccessToken映射用户成功】解析accessToken信息失败accessToken->{}",accessToken);
            return null;
        }
        Object username= jwtPayload.getClaim(TokenKeyEnum.USER.name());
        if (username==null){
            return null;
        }
        log.info(StrUtil.format("【AccessToken映射用户成功】accessToken->{},用户->{}", accessToken,username.toString()));
        return username.toString();
    }

}
