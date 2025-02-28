package com.xm.util.auth;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.LoginUserVo;
import com.xm.auth.domain.vo.TokenVo;
import com.xm.auth.domain.vo.UserVo;
import com.xm.auth.service.TcUserRoleRelService;
import com.xm.auth.service.TcUserService;
import com.xm.consts.AuthCacheKey;
import com.xm.consts.SessionAttr;
import com.xm.util.auth.params.SessionMappingData;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.cache.CacheUtil;
import com.xm.util.lock.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.Session;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class LoginSessionUtil {

    private static final TcUserService userService;

    private static final TcUserRoleRelService userRoleRelService;

    static {
        userRoleRelService=SpringBeanUtil.getBeanByClass(TcUserRoleRelService.class);
        userService= SpringBeanUtil.getBeanByClass(TcUserService.class);
    }


    public static UserVo convertToUserVo(TcUser tcUser){
        UserVo userVo = new UserVo();
        List<TcRole> roleList = userRoleRelService.getRoleListByUser(tcUser.getId());
        //角色默认值
        if (CollectionUtil.isNotEmpty(roleList)){
            List<String> roleCode = roleList.stream().map(TcRole::getRoleCode).collect(Collectors.toList());
            userVo.setRoles(roleCode);
        }else {
            userVo.setRoles(new ArrayList<>());
        }
        //初始化accessToken
        Date doRefreshTokenDate= TokenGenerateUtil.getDoRefreshTokenDate();
        TokenVo tokenVo= TokenGenerateUtil.initToken(tcUser.getUsername(),doRefreshTokenDate);
        userVo.setAccessToken(tokenVo.getAccessToken());
        userVo.setRefreshToken(tokenVo.getRefreshToken());
        userVo.setExpires(doRefreshTokenDate);
        userVo.setUsername(tcUser.getUsername());
        userVo.setUserId(tcUser.getId());
        userVo.setNickName(tcUser.getNickName());
        return userVo;
    }

    /**
     * 根据session获取当前登录用户的角色列表
     */
    public static List<TcRole> getCurrentLoginRoleBySession(){
        //允许创建session设置为false
        HttpSession session=HttpRequestUtil.getCurrentHttpSession();
        Object roleObjList;
        if (session==null){
            return new ArrayList<>();
        }else {
            roleObjList = session.getAttribute(SessionAttr.attr_roleKey);
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
     * 根据session获取用户
     */
    public static TcUser getCurrentLoginUserBySession(){
        //允许创建session设置为false
        HttpSession httpSession=HttpRequestUtil.getCurrentHttpSession();
        if (httpSession==null){
            return null;
        }else {
            return (TcUser) httpSession.getAttribute(SessionAttr.attr_userKey);
        }
    }


    /**
     * 初始化用户session
     */
    public static void initSession(TcUser tcUser){
        String username=tcUser.getUsername();
        if (StrUtil.isBlank(username)){
            throw new CommonException("初始化session失败，用户名不能为空");
        }
        Date now = new Date();
        LockUtil.lock(AuthCacheKey.getUserAndSession(username),()->{
            //密码不存储
            tcUser.setPassword(null);
            //删除已登录用户
            removeSessionByUserName(username);
            //用户名绑定sessionId
            HttpSession session = HttpRequestUtil.getCurrentHttpSessionWithCreate();
            if (session!=null){
                SessionMappingData sessionMappingData=new SessionMappingData();
                sessionMappingData.setSessionId(session.getId());
                sessionMappingData.setLoginTime(DateUtil.format(now, DatePattern.NORM_DATETIME_FORMATTER));
                CacheUtil.set(AuthCacheKey.getUserAndSession(username),sessionMappingData);
                session.setAttribute(SessionAttr.attr_userKey,tcUser);

                List<TcRole> roleList = userRoleRelService.getRoleListByUser(tcUser.getId());
                session.setAttribute(SessionAttr.attr_roleKey,roleList);
            }
            //更新最后一次登录时间
            if (StrUtil.isNotBlank(tcUser.getId())){
                TcUser temp=new TcUser();
                temp.setId(tcUser.getId());
                temp.setLastLoginTime(now);
                userService.getMapper().updateById(temp);
            }
            return null;
        },(e)->{
            String msg=StrUtil.format("初始化session失败->{}", ExceptionUtil.stacktraceToString(e));
            throw new CommonException(msg);
        });
    }


    /**
     * 获取所有已登录用户
     */
    public static Map<String, LoginUserVo> getAllLoginUserBySession(){
        Map<String,LoginUserVo> mapping=new HashMap<>();

        //先获取用户sessionId
        Set<String> userAndSessionKeys= CacheUtil.keys( AuthCacheKey.getUserAndSession("*"));
        if (CollectionUtil.isEmpty(userAndSessionKeys)){
            return mapping;
        }

        for (String userAndSessionKey:userAndSessionKeys){
            SessionMappingData sessionMappingData= (SessionMappingData) CacheUtil.get(userAndSessionKey);
            if (sessionMappingData==null){
                continue;
            }
            String sessionId = sessionMappingData.getSessionId();
            Session session=StorageSessionUtil.findSessionBySessionId(sessionId);
            if (session==null){
                continue;
            }
            TcUser tcUser= session.getAttribute(SessionAttr.attr_userKey);
            if (tcUser!=null){
                LoginUserVo loginUserVo=new LoginUserVo();
                BeanUtil.copyProperties(tcUser,loginUserVo);
                loginUserVo.setLoginTime(sessionMappingData.getLoginTime());
                loginUserVo.setSessionId(sessionMappingData.getSessionId());
                mapping.put(sessionId,loginUserVo);
            }
        }
        return mapping;
    }

    /**
     * 删除当前登录用户session
     */
    public static void removeCurrentLoginUserSession(){
        removeSessionByUserName(getUserNameBySession());
    }



    /**
     * 根据用户名删除指定用户session
     */
    public static void removeSessionByUserName(String username){
        if (StrUtil.isBlank(username)){
            return;
        }
        LockUtil.lock(AuthCacheKey.getUserAndSession(username),()->{
            SessionMappingData sessionMappingData= (SessionMappingData) CacheUtil.get(AuthCacheKey.getUserAndSession(username));
            if (sessionMappingData==null){
                return null;
            }
            String sessionId = sessionMappingData.getSessionId();
            if (StrUtil.isNotBlank(sessionId)){
                CacheUtil.delete(AuthCacheKey.getUserAndSession(username));
                StorageSessionUtil.deleteSessionBySessionId(sessionId);
            }
            //判断当前移除用户是否为自己，如果是自己则把HttpSession无效化
            String userNameBySession = getUserNameBySession();
            if (username.equals(userNameBySession)){
                HttpSession httpSession = HttpRequestUtil.getCurrentHttpSession();
                if (httpSession!=null){
                    httpSession.invalidate();
                }
            }
            return null;
        },(e)->{
            String msg=StrUtil.format("移除session失败->{}", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        });
    }


    private static String getUserNameBySession(){
        HttpSession httpSession = HttpRequestUtil.getCurrentHttpSession();
        if (httpSession!=null){
            TcUser tcUser= (TcUser) httpSession.getAttribute(SessionAttr.attr_userKey);
            if (tcUser!=null){
                return tcUser.getUsername();
            }
        }
        return null;
    }
}
