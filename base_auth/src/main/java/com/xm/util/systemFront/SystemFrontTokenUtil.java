package com.xm.util.systemFront;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.systemFront.domain.entity.TcSystemFrontToken;
import com.xm.systemFront.mapper.TcSystemFrontTokenMapper;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.cache.CacheUtil;
import com.xm.util.systemFront.params.VerifyTokenData;
import com.xm.util.systemFront.params.VerifyTokenResult;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SystemFrontTokenUtil {

    private static final TcSystemFrontTokenMapper tcSystemFrontTokenMapper;

    static {
        tcSystemFrontTokenMapper = SpringBeanUtil.getBeanByClass(TcSystemFrontTokenMapper.class);
    }


    public static VerifyTokenResult verifySystemFrontAccessToken(String accessToken) {
        VerifyTokenResult result=new VerifyTokenResult();
        if (StrUtil.isBlank(accessToken)){
            result.setSuccess(false);
            result.setErrorMsg("accessToken不能为空");
            return result;
        }
        String key="SyncFrontAccessToken:"+accessToken;
        Object object = CacheUtil.get(key);
        if (object==null){
            LambdaQueryWrapper<TcSystemFrontToken> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TcSystemFrontToken::getAccessToken,accessToken);
            TcSystemFrontToken config = tcSystemFrontTokenMapper.selectOne(lambdaQueryWrapper);
            if (config==null){
                result.setSuccess(false);
                result.setErrorMsg("鉴权失败");
                return result;
            }else {
                String name = config.getName();
                VerifyTokenData verifyTokenData=new VerifyTokenData();
                verifyTokenData.setLoginTime(new Date());
                verifyTokenData.setUserName(name);

                CacheUtil.set(key,verifyTokenData,2, TimeUnit.HOURS);

                result.setSuccess(true);
                result.setErrorMsg("鉴权成功");
                return result;
            }
        }else {
            result.setSuccess(true);
            result.setErrorMsg("鉴权成功");
            return result;
        }
    }
}
