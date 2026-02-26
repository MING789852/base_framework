package com.xm.util.http.operation;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.util.http.enums.HttpMethodEnum;
import com.xm.util.http.params.HttpOperationResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;
@Slf4j
public abstract class HttpOperation {

    public abstract HttpOperationResponse doJsonRequestReturnResponse(String url, HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap);

    public abstract String doJsonRequestReturnString(String url,HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap);

    public abstract <T> T doJsonRequestReturnObject(String url,HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap,Class<T> tClass);

    public abstract <T> T doJsonRequestReturnObject(String url, HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap, Type type);

    public abstract HttpOperationResponse doXWWWFormUrlencodedReturnResponse(String url, HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap);

    public abstract String doXWWWFormUrlencodedReturnString(String url,HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap);

    public abstract <T> T doXWWWFormUrlencodedReturnObject(String url,HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap,Class<T> tClass);

    /**
     * 构建请求url
     * @param queryMap url参数
     */
    public String buildUrl(String url, Map<String, String> queryMap) {
        StringBuilder sbUrl = new StringBuilder();
        if (!StrUtil.isBlank(url)) {
            sbUrl.append(url);
        }
        if (null != queryMap && !queryMap.isEmpty()) {
            // 判断原URL是否已包含参数（是否已包含'?'）
            boolean hasExistingParams = sbUrl.indexOf("?") > -1;
            StringBuilder sbQuery = new StringBuilder();

            for (Map.Entry<String, String> query : queryMap.entrySet()) {
                // 忽略键为空的参数
                if (StrUtil.isBlank(query.getKey())) {
                    continue;
                }
                if (sbQuery.length() > 0) {
                    sbQuery.append("&");
                }
                sbQuery.append(query.getKey());
                if (!StrUtil.isBlank(query.getValue())) {
                    sbQuery.append("=");
                    try {
                        sbQuery.append(URLEncoder.encode(query.getValue(), "utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        String msg = StrUtil.format("拼接url参数失败->{}", ExceptionUtil.stacktraceToString(e));
                        log.error(msg);
                        throw new CommonException(msg);
                    }
                }
            }

            if (sbQuery.length() > 0) {
                // 根据原URL是否有参数来决定连接符
                char connector = hasExistingParams ? '&' : '?';
                sbUrl.append(connector).append(sbQuery);
            }
        }
        return sbUrl.toString();
    }

}
