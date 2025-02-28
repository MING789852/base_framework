package com.xm.util.http.operation;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.util.http.enums.HttpMethodEnum;
import com.xm.util.http.params.HttpOperationResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@Component
@Slf4j
public class ApacheHttpOperation extends HttpOperation{
    private final static CloseableHttpClient httpClient;

    static {
        try {
            // 在调用SSL之前需要重写验证方法，取消检测SSL
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String str) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String str) {
                }
            };
            SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            ctx.init(null, new TrustManager[]{trustManager}, null);
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
            // 创建Registry
            RequestConfig requestConfig = RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.STANDARD_STRICT)
                    .setExpectContinueEnabled(Boolean.TRUE).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
                    .setProxyPreferredAuthSchemes(Collections.singletonList(AuthSchemes.BASIC)).build();
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", socketFactory).build();
            // keep-alive
            ConnectionKeepAliveStrategy keepAliveStrategy = (response, context) -> {
                HeaderElementIterator it = new BasicHeaderElementIterator
                        (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase
                            ("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return 60 * 1000;//如果没有约定，则默认定义时长为60s
            };
            // 创建ConnectionManager，添加Connection配置信息
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            //最大连接数
            connectionManager.setMaxTotal(100);
            //例如默认每路由最高50并发，具体依据业务来定
            connectionManager.setDefaultMaxPerRoute(40);

            httpClient= HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setKeepAliveStrategy(keepAliveStrategy)
                    .setDefaultRequestConfig(requestConfig)
                    .evictExpiredConnections()

                    .build();
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 设置 连接超时、 请求超时 、 读取超时  毫秒
     */
    private static RequestConfig setTimeOutConfig(RequestConfig requestConfig) {
        if (requestConfig == null) {
            requestConfig = RequestConfig.DEFAULT;
        }
        return RequestConfig.copy(requestConfig)
                .setConnectionRequestTimeout(60000)
                .setConnectTimeout(60000)
                .setSocketTimeout(60000)
                .build();
    }


    private HttpResponse doJsonRequest(String url, HttpMethodEnum methodEnum, Map<String,String> headers, String dataMap) throws IOException {
        RequestBuilder requestBuilder = RequestBuilder.create(methodEnum.name()).setUri(url);
        if (headers!=null){
            for (String header:headers.keySet()){
                requestBuilder.setHeader(header,headers.get(header));
            }
        }
        requestBuilder.setConfig(setTimeOutConfig(requestBuilder.getConfig()));
        if (dataMap!=null){
            StringEntity stringEntity=new StringEntity(dataMap, Charset.defaultCharset());
            stringEntity.setContentType(ContentType.APPLICATION_JSON.toString());
            requestBuilder.setEntity(stringEntity);
        }
        return httpClient.execute(requestBuilder.build());
    }

    private Charset getCharsetByEntity(HttpEntity entity){
        Charset charset = null;
        ContentType contentType = ContentType.get(entity);
        if (contentType != null) {
            charset = contentType.getCharset();
            if (charset == null) {
                ContentType defaultContentType = ContentType.getByMimeType(contentType.getMimeType());
                charset = defaultContentType != null ? defaultContentType.getCharset() : null;
            }
        }
        if (charset == null) {
            charset = HTTP.DEF_CONTENT_CHARSET;
        }
        return charset;
    }

    @Override
    public HttpOperationResponse doJsonRequestReturnResponse(String url, HttpMethodEnum methodEnum, Map<String, String> headers, String dataMap) {
        String format = StrUtil.format("\n url->{} \n method->{} \n head->{} \n body->{}\n", url,methodEnum.name(), headers, dataMap);
        try {
            HttpResponse httpResponse=doJsonRequest(url,methodEnum,headers,dataMap);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode== HttpStatus.SC_OK){
                HttpOperationResponse httpOperationResponse=new HttpOperationResponse();
                HttpEntity entity = httpResponse.getEntity();
                byte[] readBytes = IoUtil.readBytes(entity.getContent(),true);
                httpOperationResponse.setByteContent(readBytes);
                httpOperationResponse.setStrContent(new String(readBytes,getCharsetByEntity(entity)));
                httpOperationResponse.setHttpStatus(statusCode);
                httpOperationResponse.setHeaders(httpOperationResponse.getHeaders());
                //打印日志
                String msg = StrUtil.format("{} 请求成功,请求结果->{}", format, httpOperationResponse.getStrContent());
                log.info(msg);
                return httpOperationResponse;
            }else {
                String msg=StrUtil.format("{} statusCode->{}",format,httpResponse.getStatusLine().getStatusCode());
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
        return null;
    }
}
