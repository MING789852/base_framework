package com.xm.configuration.schedule;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class XxlJobConfig {

    private final XxlJobProperty xxlJobProperty;

    @Bean
    @ConditionalOnProperty(prefix = "xxl.job", name = "enable", havingValue = "true")
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job 配置初始化 <<<<<<<<<<<<");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(xxlJobProperty.getAdminAddress());
        xxlJobSpringExecutor.setAppname(xxlJobProperty.getExecutorAppname());
        xxlJobSpringExecutor.setIp(xxlJobProperty.getExecutorIp());
        xxlJobSpringExecutor.setPort(xxlJobProperty.getExecutorPort());
        xxlJobSpringExecutor.setAccessToken(xxlJobProperty.getAccessToken());
        xxlJobSpringExecutor.setLogPath(xxlJobProperty.getExecutorLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(xxlJobProperty.getExecutorLogretentiondays());

        return xxlJobSpringExecutor;
    }
}
