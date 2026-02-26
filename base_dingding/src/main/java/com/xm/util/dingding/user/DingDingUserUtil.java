package com.xm.util.dingding.user;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.util.dingding.DingDingUtil;
import com.xm.util.dingding.user.requestRes.DDUserDetail;
import com.xm.util.dingding.user.requestRes.DDUserInfo;
import com.xm.util.http.HttpUtils;
import com.xm.util.http.enums.HttpMethodEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DingDingUserUtil {
    /**
     * 获取用户详情
     */
    public static DDUserDetail getUserDetail(String userId){
        String url = "https://oapi.dingtalk.com/topapi/v2/user/get?access_token="+DingDingUtil.getAccessToken();
        JSONObject dataMap=new JSONObject();
        dataMap.set("userid",userId);
        Map<String,String> headers=new HashMap<>();
        headers.put("Content-Type","application/json");
        String resStr= HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, headers, dataMap.toString());
        JSONObject res= JSONUtil.parseObj(resStr);
        Integer errcode=res.getInt("errcode");
        if (errcode==0){
            JSONObject result=res.getJSONObject("result");
            return result.toBean(DDUserDetail.class);
        }else {
            String errmsg=res.getStr("errmsg");
            log.error(StrUtil.format("【请求钉钉获取用户详情失败】 {}",errmsg));
            return null;
        }
    }

    /**
     * 免登码获取用户信息
     */
    public static DDUserInfo getUserInfo(String authCode){
        String url = "https://oapi.dingtalk.com/topapi/v2/user/getuserinfo?access_token="+ DingDingUtil.getAccessToken();
        JSONObject dataMap=new JSONObject();
        dataMap.set("code",authCode);
        String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, null, JSONUtil.toJsonStr(dataMap));
        JSONObject res=JSONUtil.parseObj(resStr);
        Integer errcode=res.getInt("errcode");
        if (errcode==0){
            JSONObject result=res.getJSONObject("result");
            return result.toBean(DDUserInfo.class);
        }else {
            String errmsg=res.getStr("errmsg");
            throw new CommonException(StrUtil.format("【请求钉钉获取用户的userid失败】 {}",errmsg));
        }
    }
}
