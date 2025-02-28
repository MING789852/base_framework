package com.xm.util.http.operation;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.xm.advice.exception.exception.CommonException;
import com.xm.util.http.enums.HttpMethodEnum;
import com.xm.util.http.params.HttpOperationResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OkHttpOperation extends HttpOperation{

    /**
     * 连接超时时间
     **/
    private static final int CONNECT_TIMEOUT_TEN_SECONDS = 10;

    /**
     * 读取返回信息超时时间
     **/
    private static final int READ_TIMEOUT_THIRTY_SECONDS = 30;

    private static OkHttpClient okHttpClient;

    private static final Gson gson=new Gson();

    static {
        synchronized (OkHttpOperation.class) {
            if (okHttpClient == null) {
                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(CONNECT_TIMEOUT_TEN_SECONDS, TimeUnit.SECONDS)
                        .readTimeout(READ_TIMEOUT_THIRTY_SECONDS, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build();
            }
        }
    }

    private Response doJsonRequest(String url, HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap) throws IOException {
        Request.Builder builder = new Request.Builder();
        // 请求头处理
        if (headers != null && !headers.isEmpty()) {
            builder.headers(Headers.of(headers));
        }
        RequestBody requestBody = null;
        if (dataMap!=null){
            requestBody=RequestBody.create(dataMap, MediaType.get("application/json"));
        }
        builder.method(methodEnum.name(),requestBody);
        Request request = builder.url(url).build();
        return okHttpClient.newCall(request).execute();
    }

    private Map<String, String> headerToMap(Headers headers){
        Map<String, String> headerMap = new HashMap<>();
        for (int i = 0, size = headers.size(); i < size; i++) {
            headerMap.put(headers.name(i), headers.value(i));
        }
        return headerMap;
    }

    private Charset getCharsetByResponseBody(ResponseBody body){
        MediaType contentType = body.contentType();
        Charset charset=null;
        if (contentType!=null){
            charset=contentType.charset();
        }
        if (charset==null){
            charset=StandardCharsets.UTF_8;
        }
        return charset;
    }

    @Override
    public HttpOperationResponse doJsonRequestReturnResponse(String url, HttpMethodEnum methodEnum, Map<String, String> headers, String dataMap) {
        long startTime= System.currentTimeMillis();
        String format = StrUtil.format("\n url->{} \n method->{} \n head->{} \n body->{}\n", url,methodEnum.name(), headers, dataMap);
        try (Response response = doJsonRequest(url, methodEnum, headers, dataMap)) {
            long endTime= System.currentTimeMillis();
            long time=endTime-startTime;
            if (response.code()== HttpStatus.SC_OK){
                HttpOperationResponse httpOperationResponse=new HttpOperationResponse();
                httpOperationResponse.setHttpStatus(response.code());
                httpOperationResponse.setHeaders(headerToMap(response.headers()));
                ResponseBody body = response.body();
                if (body!=null){
                    ByteString byteString = body.byteString();
                    httpOperationResponse.setByteContent(byteString.toByteArray());
                    Charset charsetByResponseBody = getCharsetByResponseBody(body);
                    httpOperationResponse.setStrContent(byteString.string(charsetByResponseBody));
                    //打印日志
                    String msg = StrUtil.format("{} 耗时->{}ms\n 请求成功,请求结果->{}", format,time,httpOperationResponse.getStrContent());
                    log.info(msg);
                }
                return httpOperationResponse;
            }else {
                ResponseBody body = response.body();
                String errorMsg="";
                if (body!=null){
                    errorMsg=body.string();
                }
                String msg=StrUtil.format("{} statusCode->{},errorMsg->{}",format,response.code(),errorMsg);
                log.error(msg);
                throw new CommonException(msg);
            }
        } catch (IOException e) {
            String msg=StrUtil.format("{} 请求失败->{}",format, ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }
    }

    @Override
    public String doJsonRequestReturnString(String url, HttpMethodEnum methodEnum, Map<String, String> headers, String dataMap) {
        HttpOperationResponse response = doJsonRequestReturnResponse(url, methodEnum, headers, dataMap);
        return response.getStrContent();
    }

    @Override
    public <T> T doJsonRequestReturnObject(String url, HttpMethodEnum methodEnum, Map<String, String> headers, String dataMap, Class<T> tClass) {
        String res=doJsonRequestReturnString(url, methodEnum, headers, dataMap);
        try {
            return gson.fromJson(res,tClass);
//            return JSONUtil.toBean(res,tClass);
        }catch (Exception e){
            String msg=StrUtil.format("请求返回转换为->{}失败,原因->{}",tClass.getName(),ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }
    }
}
