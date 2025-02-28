package com.xm.util.http.operation;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.util.http.enums.HttpMethodEnum;
import com.xm.util.http.params.HttpOperationResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
@Slf4j
public abstract class HttpOperation {

    public abstract HttpOperationResponse doJsonRequestReturnResponse(String url, HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap);

    public abstract String doJsonRequestReturnString(String url,HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap);

    public abstract <T> T doJsonRequestReturnObject(String url,HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap,Class<T> tClass);

    /**
     * 构建请求url
     * @param queryMap url参数
     */
    public String buildUrl(String url, Map<String, String> queryMap) {
        StringBuilder sbUrl = new StringBuilder();
        if (!StrUtil.isBlank(url)) {
            sbUrl.append(url);
        }
        if (null != queryMap) {
            StringBuilder sbQuery = new StringBuilder();
            for (Map.Entry<String, String> query : queryMap.entrySet()) {
                if (0 < sbQuery.length()) {
                    sbQuery.append("&");
                }
                if (StrUtil.isBlank(query.getKey()) && !StrUtil.isBlank(query.getValue())) {
                    sbQuery.append(query.getValue());
                }
                if (!StrUtil.isBlank(query.getKey())) {
                    sbQuery.append(query.getKey());
                    if (!StrUtil.isBlank(query.getValue())) {
                        sbQuery.append("=");
                        try {
                            sbQuery.append(URLEncoder.encode(query.getValue(), "utf-8"));
                        } catch (UnsupportedEncodingException e) {
                            String msg=StrUtil.format("拼接url参数失败->{}", ExceptionUtil.stacktraceToString(e));
                            log.error(msg);
                            throw new CommonException(msg);
                        }
                    }
                }
            }
            if (0 < sbQuery.length()) {
                sbUrl.append("?").append(sbQuery);
            }
        }
        return sbUrl.toString();
    }

}
