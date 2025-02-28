package com.xm.configuration.dingding;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
public class DingdingProperty {
    private String appKey;
    private String appSecret;
    private String agentId;
    private String corpId;
    private String authUrl;
    private String resultUrl;
    private String robotCode;
}
