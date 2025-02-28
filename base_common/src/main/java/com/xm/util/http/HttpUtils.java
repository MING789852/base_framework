package com.xm.util.http;

import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.http.enums.HttpMethodEnum;
import com.xm.util.http.operation.HttpOperation;
import com.xm.util.http.operation.OkHttpOperation;
import com.xm.util.http.params.HttpOperationResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public  class HttpUtils {

    private final static HttpOperation httpOperation= SpringBeanUtil.getBeanByClass(OkHttpOperation.class);


    /**
     * post请求返回HttpResponse格式
     * @param url post请求url
     * @param headers  post请求头
     * @param dataMap post请求体
     */
    public static HttpOperationResponse doJsonRequestReturnResponse(String url, HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap){
        return httpOperation.doJsonRequestReturnResponse(url, methodEnum, headers, dataMap);
    }

    /**
     * post请求返回json格式
     * @param url post请求url
     * @param headers  post请求头
     * @param dataMap post请求体
     */
    public static String doJsonRequestReturnString(String url,HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap){
        return httpOperation.doJsonRequestReturnString(url, methodEnum, headers, dataMap);
    }

    public static <T> T doJsonRequestReturnObject(String url,HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap,Class<T> tClass){
        return httpOperation.doJsonRequestReturnObject(url, methodEnum, headers, dataMap,tClass);
    }

    /**
     * 构建请求url
     * @param queryMap url参数
     */
    public static String buildUrl(String url, Map<String, String> queryMap) {
        return httpOperation.buildUrl(url, queryMap);
    }
}
