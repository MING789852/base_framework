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
}
