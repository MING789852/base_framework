package com.xm.configuration.schedule;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "xxl.job")
@Data
public class XxlJobProperty {
    private boolean enable;
    private String adminAddress;
    private String accessToken;
    private String executorAppname;
    private String executorAddress;
    private String executorIp;
    private Integer executorPort;
    private String executorLogPath;
    private Integer executorLogretentiondays;
}
