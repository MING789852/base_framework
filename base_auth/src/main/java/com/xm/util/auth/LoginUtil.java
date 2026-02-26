package com.xm.util.auth;

import cn.hutool.core.date.DateUtil;
import com.xm.util.auth.params.LoginTry;
import com.xm.util.cache.CacheUtil;
import com.xm.util.lock.LockUtil;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LoginUtil {
    public static final String LoginTryCountKey = "Login_Try_Count";
    public static final Integer LoginTryCountMax=5;
    //单位分钟
    public static final Integer LoginTryCountTime=10;

    private static String getLoginTryCountKeyByUsername(String username){
        return LoginTryCountKey+":"+username;
    }

    public static void  addTryCount(String username){
        if (username == null){
            return;
        }
        String loginTryCountKey = getLoginTryCountKeyByUsername(username);
        LockUtil.lock(loginTryCountKey, ()->{
            LoginTry loginTry = CacheUtil.get(loginTryCountKey, LoginTry.class);
            if(loginTry == null){
                loginTry=new LoginTry();
            }
            loginTry.setCount(loginTry.getCount()+1);
            loginTry.setLastTryTime(DateUtil.formatTime(new Date()));
            CacheUtil.set(loginTryCountKey, loginTry,LoginTryCountTime, TimeUnit.MINUTES);
            return null;
        },null);
    }

    public static boolean allowTry(String username){
        if (username == null){
            return false;
        }
        String loginTryCountKey = getLoginTryCountKeyByUsername(username);
        return LockUtil.lock(loginTryCountKey, ()->{
            LoginTry loginTry = CacheUtil.get(loginTryCountKey, LoginTry.class);
            if(loginTry == null){
                return true;
            }else {
                return loginTry.getCount()<LoginTryCountMax;
            }
        },null);
    }

    public static void resetTryCount(String username){
        if (username == null){
            return;
        }
        String loginTryCountKey = getLoginTryCountKeyByUsername(username);
        LockUtil.lock(loginTryCountKey, ()->{
            CacheUtil.delete(loginTryCountKey);
            return null;
        },null);
    }
}
