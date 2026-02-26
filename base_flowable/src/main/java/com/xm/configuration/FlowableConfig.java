package com.xm.configuration;

import cn.hutool.core.collection.CollectionUtil;
import com.xm.flowable.function.FlowableBeanFunction;
import com.xm.util.bean.SpringBeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.el.ProcessExpressionManager;
import org.flowable.engine.impl.persistence.deploy.DeploymentManager;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
@DependsOn(value = {"SpringBeanUtil"})
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {


//    private final SqlSessionFactory sqlSessionFactory;

    private final DataSource dataSource;

//    public DataSource getDataSource(){
//        log.info("流程引擎初始化,使用数据源tckj=======================");
//        Map<String,DataSource> dataSourceMap = SpringBeanUtil.getBeanByName("dataSourceMap", Map.class);
//        return dataSourceMap.get("tckj");
//    }


    @Bean
    public ExpressionManager expressionManager(){
        ProcessExpressionManager processExpressionManager = new ProcessExpressionManager();
        Map<String, FlowableBeanFunction> springFlowableBeanFunctionMap= SpringBeanUtil.getBeanMapOfType(FlowableBeanFunction.class);
        if (CollectionUtil.isNotEmpty(springFlowableBeanFunctionMap)){
            Map<Object, Object> beanMap = new HashMap<>(springFlowableBeanFunctionMap);
            processExpressionManager.setBeans(beanMap);
        }
        return processExpressionManager;
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