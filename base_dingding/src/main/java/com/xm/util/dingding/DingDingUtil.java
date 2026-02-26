package com.xm.util.dingding;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xm.configuration.dingding.DingdingConfigContextHolder;
import com.xm.configuration.dingding.DingdingProperty;
import com.xm.configuration.dingding.KeyDingdingProperty;
import com.xm.util.cache.CacheUtil;
import com.xm.util.http.HttpUtils;
import com.xm.util.http.enums.HttpMethodEnum;

import java.util.concurrent.TimeUnit;

public class DingDingUtil {

    private final static String redisKey="DD_ACCESSTOKEN";

    public static KeyDingdingProperty  getCurrentKeyAndDingdingProperty(){
        return DingdingConfigContextHolder.getCurrentKeyAndDingdingProperty();
    }

    public static DingdingProperty getCurrentDingdingProperty(){
        KeyDingdingProperty currentKeyAndDingdingProperty = getCurrentKeyAndDingdingProperty();
        return currentKeyAndDingdingProperty.getProperty();
    }

    private static String getRedisKey(){
        KeyDingdingProperty keyDingdingProperty = getCurrentKeyAndDingdingProperty();
        return redisKey+"_"+keyDingdingProperty.getKey();
    }

    /**
     * 存储redis，一小时刷新一次
     */
    public static String getAccessToken()  {
        String accessToken;
        accessToken= (String) CacheUtil.get(getRedisKey());
        if (accessToken!=null){
            return accessToken;
        }else {
            JSONObject dataMap=new JSONObject();
            DingdingProperty dingdingProperty = getCurrentDingdingProperty();
            dataMap.set("appKey",dingdingProperty.getAppKey());
            dataMap.set("appSecret",dingdingProperty.getAppSecret());
            String resStr= HttpUtils.doJsonRequestReturnString("https://api.dingtalk.com/v1.0/oauth2/accessToken", HttpMethodEnum.POST, null, dataMap.toString());
            JSONObject jsonObject=JSONUtil.parseObj(resStr);
            accessToken=jsonObject.getStr("accessToken");
            //缓存一小时
            CacheUtil.set(getRedisKey(),accessToken,1, TimeUnit.HOURS);
            return accessToken;
        }
    }
}
