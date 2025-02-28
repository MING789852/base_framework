package com.xm.configuration.minio;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "file",name = "type",havingValue = "minio")
public class MinioConfig {
    private final MinioProp minioProp;

    @Bean("minioClient")
    public MinioClient minioClient() {
        return MinioClient.builder().endpoint(minioProp.getEndpoint()).
                credentials(minioProp.getAccesskey(),minioProp.getSecretkwy()).build();
    }

    @Bean("multipartMinioClient")
    @ConditionalOnMissingBean({ MultipartMinioClient.class })
    public MultipartMinioClient multipartMinioClient(MinioClient minioClient){
        try {
            Field field = minioClient.getClass().getDeclaredField("asyncClient");
            field.setAccessible(true);
            return new MultipartMinioClient((MinioAsyncClient) field.get(minioClient));
        } catch (Throwable ex) {
            String msg=StrUtil.format("创建自定义MinioAsyncClient失败->{}", ExceptionUtil.stacktraceToString(ex));
            log.error(msg);
            throw new CommonException(msg);
        }
    }
}
