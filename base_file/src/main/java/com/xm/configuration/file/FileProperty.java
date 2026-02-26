package com.xm.configuration.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
@Data
public class FileProperty {
    // local、minio
    private String type;
    private String path;
    private Boolean auth;
    //是否允许重复上传(md5)
    //【不允许重复上传】使用MD5校验文件是否存在
    //【允许重复上传】不使用MD5校验文件是否存在
    //默认不允许重复上传
    private Boolean allowRepeatUpload=false;
}
