package com.xm.configuration.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProp {
    //连接url
    private String endpoint;
    //公钥
    private String accesskey;
    //私钥
    private  String secretkwy;
}