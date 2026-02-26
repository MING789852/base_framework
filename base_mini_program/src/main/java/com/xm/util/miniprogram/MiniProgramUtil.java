package com.xm.util.miniprogram;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.xm.advice.exception.exception.CommonException;
import com.xm.configuration.wechat.miniprogram.MiniprogramConfig;
import com.xm.util.cache.CacheUtil;
import com.xm.util.retry.RetryUtil;
import com.xm.util.retry.params.RetryResult;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.http.HttpUtils;
import com.xm.util.http.enums.HttpMethodEnum;
import com.xm.util.http.params.HttpOperationResponse;
import com.xm.util.miniprogram.res.*;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.util.StringUtil;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class MiniProgramUtil {
    private final static MiniprogramConfig miniprogramconfig;

    private final static AES aes;


    private final static String AesKey = "tckj154xnbna9uky";

    private final static String AesIv = "tckj154xnbna9uiv";

    private final static String accessTokenKey="miniAccessToken";


    static {
        miniprogramconfig= SpringBeanUtil.getBeanByClass(MiniprogramConfig.class);
        aes = new AES("CBC", Padding.ZeroPadding.name(),
                // 密钥，可以自定义
                AesKey.getBytes(),
                // iv加盐，按照实际需求添加
                AesIv.getBytes());
    }

    public static String decryptParam(String encryptedParam) {
        return aes.decryptStr(encryptedParam);
    }

    public static String encryptParam(String param) {
        return aes.encryptBase64(param);
    }

    private static String getRedisKey(String openId,String key){
        return StrUtil.format("{}_{}",openId,key);
    }

    public static boolean putStoreData(String openId,String key, Object data){
        if (StrUtil.isBlank(openId)){
            return false;
        }
        if(StrUtil.isBlank(key)){
            return  false;
        }
        if (data==null){
            return  false;
        }
        CacheUtil.put(openId,key,data);
        return true;
    }

    public static <T> T getStoreData(String openId,String key,Class<T> tClass) {
        return CacheUtil.get(openId,key,tClass);
    }


    public static String getCurrentOpenId(){
        RequestAttributes  requestAttributes=RequestContextHolder.getRequestAttributes();
        if (requestAttributes==null){
            return null;
        }
        Object openIdObj=requestAttributes.getAttribute(MiniprogramConfig.getOpenIdKey(),RequestAttributes.SCOPE_REQUEST);
        return openIdObj==null?null:openIdObj.toString();
    }

    public static String getRequestId(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        if (requestAttributes==null){
            return null;
        }
        HttpServletRequest request = requestAttributes.getRequest();
        return request.getHeader("requestId");
    }

    public static <T> T  limitRequest(Supplier<T> supplier){
        String requestId =getRequestId();
        if (StrUtil.isBlank(requestId)){
            log.error("请求头错误");
            throw new CommonException("请求头错误");
        }
        Integer frequency=(Integer)CacheUtil.get(requestId);
        if (frequency==null){
            frequency = 0;
            CacheUtil.set(requestId,0,15, TimeUnit.MINUTES);
        }else{
            if (frequency>=3){
                log.error("requestId->{},多次重复请求失败，已锁定请求，请15分钟后重试",requestId);
                throw new CommonException("多次重复请求失败，已锁定请求，请15分钟后重试");
            }
        }
        try {
            return supplier.get();
        }catch (Exception e){
            CacheUtil.set(requestId,frequency+1,15,TimeUnit.MINUTES);
            throw new CommonException(e.getMessage());
        }
    }

    public static String getAccessToken(){
        String tag = "请求微信小程序getAccessToken";
        return RetryUtil.retry((context) -> {
            String url = "https://api.weixin.qq.com/cgi-bin/token";
            Object accessTokenObj = CacheUtil.get(accessTokenKey);
            if (accessTokenObj == null) {
                Map<String,String> queryMap = new HashMap<>();
                queryMap.put("appid",miniprogramconfig.getAppID());
                queryMap.put("secret",miniprogramconfig.getAppSecret());
                queryMap.put("grant_type","client_credential");
                url= HttpUtils.buildUrl(url,queryMap);
                String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.GET,null,null);
                JSONObject res= JSONUtil.parseObj(resStr);
                AccessToken accessToken=res.toBean(AccessToken.class);
                /*
                  错误码	错误描述	解决方案
                  -1	system error	系统繁忙，此时请开发者稍候再试
                  40001	invalid credential  access_token isinvalid or not latest	获取 access_token 时 AppSecret 错误，或者 access_token 无效。请开发者认真比对 AppSecret 的正确性，或查看是否正在为恰当的公众号调用接口
                  40013	invalid appid	不合法的 AppID ，请开发者检查 AppID 的正确性，避免异常字符，注意大小写
                  40002	invalid grant_type	不合法的凭证类型
                  40125	不合法的 secret	请检查 secret 的正确性，避免异常字符，注意大小写
                  40164	调用接口的IP地址不在白名单中	请在接口IP白名单中进行设置
                  41004	appsecret missing	缺少 secret 参数
                  50004	禁止使用 token 接口
                  50007	账号已冻结
                  61024	第三方平台 API 需要使用第三方平台专用 token
                  40243	AppSecret已被冻结，请登录小程序平台解冻后再次调用。
                 */
                if (accessToken.getErrcode()==0){
                    CacheUtil.set(accessTokenKey,accessToken.getAccess_token(),1,TimeUnit.HOURS);
                    return RetryResult.success(accessToken.getAccess_token());
                } else if (Objects.equals(-1, accessToken.getErrcode())){
                    return RetryResult.failRetry(accessToken.getErrmsg());
                }else {
                    return RetryResult.failStop(accessToken.getErrmsg());
                }
            }else {
                return RetryResult.success(accessTokenObj.toString());
            }
        }, tag, null);

    }

    /**
     * @param js_code wx.login获取的code值
     * @return JsCode2Session
     */
    public static JsCode2Session requsetJSCode2session(String js_code){
        String tag = "请求微信小程序JsCode2session";
        return RetryUtil.retry((context)->{
            Map<String,String> queryMap = new HashMap<>();
            queryMap.put("appid",miniprogramconfig.getAppID());
            queryMap.put("secret",miniprogramconfig.getAppSecret());
            queryMap.put("js_code",js_code);
            queryMap.put("grant_type","authorization_code");
            String url=HttpUtils.buildUrl("https://api.weixin.qq.com/sns/jscode2session",queryMap);
            String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.GET,null,null);
            JSONObject res= JSONUtil.parseObj(resStr);
            JsCode2Session jsCode2Session=res.toBean(JsCode2Session.class);
            /*
              错误码	错误码取值	解决方案
              40029	code 无效	js_code无效
              45011	api minute-quota reach limit  mustslower  retry next minute	API 调用太频繁，请稍候再试
              40226	code blocked	高风险等级用户，小程序登录拦截 。风险等级详见用户安全解方案
              -1	system error	系统繁忙，此时请开发者稍候再试
             */
            if (jsCode2Session.getErrcode()==0){
                return RetryResult.success(jsCode2Session);
            } else if(Arrays.asList(-1,45011).contains(jsCode2Session.getErrcode())) {
                return RetryResult.failRetry(jsCode2Session.getErrmsg());
            }else {
                return RetryResult.failStop(jsCode2Session.getErrmsg());
            }
        }, tag,null);
    }


    public static String getwxacodeunlimitToBase64(MiniUnLimitQrCode miniUnLimitQrCode){
        try {
            if (StrUtil.isBlank(miniUnLimitQrCode.getPage())&& StringUtil.isBlank(miniUnLimitQrCode.getScene())){
                throw new CommonException("参数不能为空");
            }
            miniUnLimitQrCode.setEnv_version(miniprogramconfig.getEnvVersion());
            String key = StrUtil.format("{}:{}:{}_{}",
                    "WxUnLimitBase64QrCode",miniUnLimitQrCode.getEnv_version(),miniUnLimitQrCode.getPage(),miniUnLimitQrCode.getScene());
            Object base64Obj = CacheUtil.get(key);
            if (base64Obj==null){
                String base64=Base64.encode(getwxacodeunlimit(miniUnLimitQrCode));
                CacheUtil.set(key,base64);
                return base64;
            }else {
                return base64Obj.toString();
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }


    public static byte[] getwxacodeunlimit(MiniUnLimitQrCode miniUnLimitQrCode){
        String tag = "请求微信小程序getwxacodeunlimit";
        return RetryUtil.retry((context)->{
            String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit";
            String accessToken = getAccessToken();
            Map<String,String> queryMap = new HashMap<>();
            queryMap.put("access_token",accessToken);
            url = HttpUtils.buildUrl(url,queryMap);

            HttpOperationResponse response=HttpUtils.doJsonRequestReturnResponse(url,HttpMethodEnum.POST,null,JSONUtil.toJsonStr(miniUnLimitQrCode));
            if (response!=null){
                Map<String, String> headers = response.getHeaders();
                if (CollectionUtil.isNotEmpty(headers)) {
                    String contentType = headers.get("Content-Type");
                    if (StrUtil.isNotBlank(contentType)){
                        if (!contentType.contains("json")) {
                            return RetryResult.success(response.getByteContent());
                        }
                    }
                }
                String resStr=response.getStrContent();
                JSONObject res= JSONUtil.parseObj(resStr);
                BaseRes baseRes=res.toBean(BaseRes.class);
                if(Objects.equals(-1, baseRes.getErrcode())) {
                    return RetryResult.failRetry(baseRes.getErrmsg());
                }else {
                    return RetryResult.failStop(baseRes.getErrmsg());
                }
            }else {
                String msg = StrUtil.format("【{}】请求结果为null，请检查网络",tag);
                return RetryResult.failStop(msg);
            }
        }, tag,null);
    }

}
