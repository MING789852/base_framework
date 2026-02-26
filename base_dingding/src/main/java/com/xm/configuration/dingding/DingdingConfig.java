package com.xm.configuration.dingding;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "dingding")
@Data
public class DingdingConfig {
    private String resultPath;
    private String authPath;
    private Map<String,DingdingProperty> dingdingPropertyMap;
}
