package com.xm.configuration;

import com.xm.util.bean.SpringBeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.el.ProcessExpressionManager;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@Slf4j
@DependsOn(value = {"SpringBeanUtil"})
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    private final DataSource dataSource;

    @Bean
    public ExpressionManager expressionManager(){
        return new ProcessExpressionManager();
    }

    @Override
    public void configure(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
        springProcessEngineConfiguration.setDataSource(dataSource);
        //事务管理器
        PlatformTransactionManager beanByClass = SpringBeanUtil.getBeanByClass(PlatformTransactionManager.class);
        springProcessEngineConfiguration.setTransactionManager(beanByClass);
//        springProcessEngineConfiguration.setSqlSessionFactory(sqlSessionFactory);

        ExpressionManager expressionManager = SpringBeanUtil.getBeanByName("expressionManager",ExpressionManager.class);
        springProcessEngineConfiguration.setExpressionManager(expressionManager);

        springProcessEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    }
}