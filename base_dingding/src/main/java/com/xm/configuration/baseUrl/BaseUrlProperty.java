package com.xm.configuration.baseUrl;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "baseurl")
@Data
public class BaseUrlProperty {
    private String fgUrl;
    private String bgUrl;
    private String callBackUrl;
}
