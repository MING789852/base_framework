package com.xm.util.auth;

import cn.hutool.core.collection.CollectionUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.advice.exception.exception.UnAuthException;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcRouter;
import com.xm.auth.domain.entity.TcUser;
import com.xm.consts.AuthCacheKey;
import com.xm.consts.SessionAttr;
import com.xm.util.auth.params.RequestAction;
import com.xm.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UserInfoUtil {

    /**
     * 判断是否有指定路由操作权限
     */
    public static boolean haveRouterAction(String routerName,String actionName){
        if (isSuperPrivilege()){
            return true;
        }
        Map<String,String> currentRouterActionByRouterName = getCurrentRouterActionByRouterName(routerName);
        return currentRouterActionByRouterName.containsKey(actionName);
    }

    public static boolean haveRoleCode(List<String> roleCodeList){
        if (isSuperPrivilege()){
            return true;
        }
        List<TcRole> currentLoginRoleList = getCurrentLoginRoleBySessionOrToken();
        if (CollectionUtil.isEmpty(currentLoginRoleList)){
            return false;
        }
        if (CollectionUtil.isEmpty(roleCodeList)){
            return true;
        }
        return currentLoginRoleList.stream().anyMatch(role -> roleCodeList.contains(role.getRoleCode()));
    }

    /**
     * 获取特定路由操作的权限
     */
    public static Map<String,String> getCurrentRouterActionByRouterName(String routerName){
        Map<String, Map<String, String>> routerActionMapping = getCurrentRouterActionMapping();
        if (routerActionMapping.containsKey(routerName)){
            Map<String, String> map = routerActionMapping.get(routerName);
            if (CollectionUtil.isEmpty(map)){
                map=new HashMap<>();
            }
            return map;
        }
        return new HashMap<>();
    }

    /**
     * 获取路由操作映射
     * 外层key是路由编码(名称),内层key是路由操作编码，value是路由操作名称
     */
    public static Map<String, Map<String, String>> getCurrentRouterActionMapping(){
        HttpSession currentHttpSession = HttpRequestUtil.getCurrentHttpSession();
        if (currentHttpSession!=null){
            Object attribute = currentHttpSession.getAttribute(SessionAttr.attr_routerActionMapKey);
            if (attribute instanceof Map<?,?>){
                @SuppressWarnings("unchecked")
                Map<String, Map<String, String>> currentRouterActionMapping= (Map<String, Map<String, String>>) attribute;
                return currentRouterActionMapping;
            }
        }
        return new HashMap<>();
    }

    /**
     * 缓存路由操作映射
     * 外层key是路由编码(名称),内层key是路由操作编码，value是路由操作名称
     */
    public static void initCurrentRouterActionMapping(Map<String, Map<String, String>> currentRouterActionMapping){
        HttpSession currentHttpSession = HttpRequestUtil.getCurrentHttpSession();
        if (currentHttpSession!=null){
            currentHttpSession.setAttribute(SessionAttr.attr_routerActionMapKey,currentRouterActionMapping);
        }
    }


    public static void initCurrentRouter(List<TcRouter> routerList){
        HttpSession currentHttpSession = HttpRequestUtil.getCurrentHttpSession();
        if (currentHttpSession!=null){
            currentHttpSession.setAttribute(SessionAttr.attr_routerKey,routerList);
        }
    }

    public static void initCurrentRole(List<TcRole> roleList){
        HttpSession currentHttpSession = HttpRequestUtil.getCurrentHttpSession();
        if (currentHttpSession!=null){
            currentHttpSession.setAttribute(SessionAttr.attr_roleKey,roleList);
        }
    }

    public static void initCurrentUser(TcUser user){
        HttpSession currentHttpSession = HttpRequestUtil.getCurrentHttpSession();
        if (currentHttpSession!=null){
            currentHttpSession.setAttribute(SessionAttr.attr_userKey,user);
        }
    }

    /**
     * 判断是否超级管理员
     */
    public static boolean isSuperPrivilege(){
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，无权限操作");
        }
        return "admin".equals(currentLoginUserBySession.getUsername());
    }

    /**
     * 根据sessionId获取访问记录
     */
    public static List<RequestAction> getRequestActionByUserName(String username){
        @SuppressWarnings("unchecked")
        List<RequestAction> requestActionList = (List<RequestAction>) CacheUtil.get(AuthCacheKey.getRequestAction(username));
        if (requestActionList==null){
            requestActionList=new ArrayList<>();
        }
        return requestActionList;
    }

    /**
     * 添加访问记录
     */
    public static void addRequestAction(RequestAction requestAction,TcUser user){
        String requestActionKey = AuthCacheKey.getRequestAction(user.getUsername());
        @SuppressWarnings("unchecked")
        List<RequestAction> requestActionList = (List<RequestAction>) CacheUtil.get(requestActionKey);
        if (requestActionList==null){
            requestActionList=new ArrayList<>();
        }
        requestActionList.add(requestAction);
        //时间最好与session保持一致
        CacheUtil.set(requestActionKey,requestActionList,2, TimeUnit.HOURS);
    }

    /**
     * 获取当前登录用户(非NULL)
     */
    public static TcUser getCurrentLoginUserBySessionOrTokenNotNull(){
        TcUser currentLoginUserBySession = getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            log.error("【获取用户失败】accessToken或session都不存在登录用户");
            throw new UnAuthException("未登录");
        }
        return currentLoginUserBySession;
    }

    /**
     * (根据accessToken或session)获取当前登录用户
     */
    public static TcUser getCurrentLoginUserBySessionOrToken(){
        TcUser userBySession = LoginSessionUtil.getCurrentLoginUserBySession();
        if (userBySession!=null){
            return userBySession;
        }
        return TokenSessionUtil.getCurrentLoginUserByAccessToken();
    }

    /**
     * (根据accessToken或session)获取当前登录用户的角色列表
     */
    public static List<TcRole> getCurrentLoginRoleBySessionOrToken(){
        List<TcRole> currentLoginRoleBySession = LoginSessionUtil.getCurrentLoginRoleBySession();
        if (CollectionUtil.isNotEmpty(currentLoginRoleBySession)){
            return currentLoginRoleBySession;
        }
        return TokenSessionUtil.getCurrentLoginRoleByAccessToken();
    }


    /**
     * 判断是否登录
     */
    public static boolean isLogin(){
        return UserInfoUtil.getCurrentLoginUserBySessionOrToken() != null;
    }
}
