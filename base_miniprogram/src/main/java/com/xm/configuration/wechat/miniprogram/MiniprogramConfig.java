package com.xm.configuration.wechat.miniprogram;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechat.miniprogram")
@Data
@Slf4j
public class MiniprogramConfig {
    private String appID;
    private String appSecret;
    //正式版为 "release"，体验版为 "trial"，开发版为 "develop"
    private String envVersion;
    @Getter
    private static String headerKey = "miniAccessToken";
    @Getter
    private static String paramKey="miniAccessToken";
    @Getter
    private static String openIdKey = "openId";
}
