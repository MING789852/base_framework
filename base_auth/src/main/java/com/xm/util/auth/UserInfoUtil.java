package com.xm.util.auth;

import cn.hutool.core.collection.CollectionUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.advice.exception.exception.UnAuthException;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcRouter;
import com.xm.auth.domain.entity.TcRouterAction;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.service.TcRouterActionService;
import com.xm.auth.service.TcRouterService;
import com.xm.consts.AuthCacheKey;
import com.xm.util.auth.params.RequestAction;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class UserInfoUtil {

    private static final TcRouterService routerService;

    private static final TcRouterActionService routerActionService;


    static {
        routerService=SpringBeanUtil.getBeanByClass(TcRouterService.class);
        routerActionService=SpringBeanUtil.getBeanByClass(TcRouterActionService.class);
    }

    /**
     * 获取当前路由操作的权限
     */
    public static List<TcRouterAction> getCurrentRouterActionByRouterName(String routerName){
        TcRouter tcRouterByRouterName = routerService.getTcRouterByRouterName(routerName);
        List<TcRole> currentLoginRoleBySession = getCurrentLoginRoleBySessionOrToken();
        if (tcRouterByRouterName==null){
            return new ArrayList<>();
        }
        if (CollectionUtil.isEmpty(currentLoginRoleBySession)){
            return new ArrayList<>();
        }
        List<String> roleIdList = currentLoginRoleBySession.stream().map(TcRole::getId).collect(Collectors.toList());
        List<String> routerIdList= Collections.singletonList(tcRouterByRouterName.getId());
        return routerActionService.getRouterActionByRoleAndRouter(roleIdList,routerIdList);
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
