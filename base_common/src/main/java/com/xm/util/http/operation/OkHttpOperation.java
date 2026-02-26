package com.xm.util.http.operation;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.xm.advice.exception.exception.CommonException;
import com.xm.util.http.enums.HttpMethodEnum;
import com.xm.util.http.params.HttpOperationResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;
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
    private static final int CONNECT_TIMEOUT = 20;

    /**
     * 读取写入信息超时时间，20分钟
     **/
    private static final int READ_TIMEOUT = 20*60;
    private static final int WRITE_TIMEOUT = 20*60;

    private static OkHttpClient okHttpClient;

    private static final Gson gson=new Gson();

    static {
        synchronized (OkHttpOperation.class) {
            if (okHttpClient == null) {
                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build();
            }
        }
    }

    private Response doRequest(String url, HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap,String contentType) throws IOException {
        Request.Builder builder = new Request.Builder();
        // 请求头处理
        if (headers==null){
            headers=new HashMap<>();
        }
        for (Map.Entry<String, String> entry : headers.entrySet()){
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        RequestBody requestBody = null;
        if (dataMap!=null){
            if ("application/json".equals(contentType)){
                requestBody=RequestBody.create(dataMap, MediaType.get("application/json"));
            }
            if ("application/x-www-form-urlencoded".equals(contentType)){
                FormBody.Builder formBodyBuilder = new FormBody.Builder();
                JSONObject jsonObject = JSONUtil.parseObj(dataMap);
                for (Map.Entry<String, Object> entry : jsonObject.entrySet()){
                    Object value = entry.getValue();
                    if (value==null){
                        continue;
                    }
                    formBodyBuilder.add(entry.getKey(), value.toString());
                }
                requestBody=formBodyBuilder.build();
            }

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

    private HttpOperationResponse doRequestReturnResponse(String url, HttpMethodEnum methodEnum, Map<String, String> headers,
                                                              String dataMap,String contentType){
        long startTime= System.currentTimeMillis();
        if (headers==null){
            headers=new HashMap<>();
        }
        headers.put("Content-Type", contentType);
        String format = StrUtil.format("\n url->{} \n method->{} \n head->{} \n body->{}\n", url,methodEnum.name(), headers, dataMap);
        try (Response response = doRequest(url, methodEnum, headers, dataMap, contentType)) {
            long endTime= System.currentTimeMillis();
            long time=endTime-startTime;
            int httpStatusCode = response.code();
            HttpOperationResponse httpOperationResponse=new HttpOperationResponse();
            httpOperationResponse.setHttpStatus(httpStatusCode);
            httpOperationResponse.setHeaders(headerToMap(response.headers()));
            ResponseBody body = response.body();
            if (body!=null){
                //一次性流只读取一次,body.bytes()、body.string()、body.byteString()互斥
                ByteString byteString = body.byteString();
                httpOperationResponse.setByteContent(byteString.toByteArray());
                Charset charsetByResponseBody = getCharsetByResponseBody(body);
                httpOperationResponse.setStrContent(byteString.string(charsetByResponseBody));
                //打印日志
                String msg = StrUtil.format("{} HttpStatusCode->{}\n 耗时->{}ms\n 请求结果->{}", format, httpStatusCode,time,httpOperationResponse.getStrContent());
                log.info(msg);
            }
            return httpOperationResponse;
        } catch (IOException e) {
            String msg=StrUtil.format("{} 请求失败->{}",format, ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }
    }

    @Override
    public HttpOperationResponse doJsonRequestReturnResponse(String url, HttpMethodEnum methodEnum, Map<String, String> headers, String dataMap) {
        return doRequestReturnResponse(url, methodEnum, headers, dataMap, "application/json");
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
        }catch (Exception e){
            String msg=StrUtil.format("请求返回转换为->{}失败,原因->{}",tClass.getName(),ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }
    }

    @Override
    public <T> T doJsonRequestReturnObject(String url, HttpMethodEnum methodEnum, Map<String, String> headers, String dataMap, Type type) {
        String res=doJsonRequestReturnString(url, methodEnum, headers, dataMap);
        try {
            return gson.fromJson(res,type);
        }catch (Exception e){
            String msg=StrUtil.format("请求返回转换为->{}失败,原因->{}",type.getTypeName(),ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }
    }

    @Override
    public HttpOperationResponse doXWWWFormUrlencodedReturnResponse(String url, HttpMethodEnum methodEnum, Map<String, String> headers, String dataMap) {
        return doRequestReturnResponse(url, methodEnum, headers, dataMap, "application/x-www-form-urlencoded");
    }

    @Override
    public String doXWWWFormUrlencodedReturnString(String url, HttpMethodEnum methodEnum, Map<String, String> headers, String dataMap) {
        HttpOperationResponse response = doXWWWFormUrlencodedReturnResponse(url, methodEnum, headers, dataMap);
        return response.getStrContent();
    }

    @Override
    public <T> T doXWWWFormUrlencodedReturnObject(String url, HttpMethodEnum methodEnum, Map<String, String> headers, String dataMap, Class<T> tClass) {
        String res=doXWWWFormUrlencodedReturnString(url, methodEnum, headers, dataMap);
        try {
            return gson.fromJson(res,tClass);
        }catch (Exception e){
            String msg=StrUtil.format("请求返回转换为->{}失败,原因->{}",tClass.getName(),ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }
    }

}
