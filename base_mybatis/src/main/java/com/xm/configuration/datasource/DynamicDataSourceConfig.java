package com.xm.configuration.datasource;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.provider.AbstractDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DynamicDataSourceConfig{
    private final DefaultDataSourceCreator dataSourceCreator;
    private final DynamicDataSourceProperty dataSourceProperty;

    @Bean
    public DynamicDataSourceProvider dynamicDataSourceProvider(@Qualifier("dataSourceMap") Map<String,DataSource> dataSourceMap) {
        return new AbstractDataSourceProvider(dataSourceCreator) {
            @Override
            public Map<String, DataSource> loadDataSources() {
                return dataSourceMap;
            }
        };
    }

    @Bean
    @Primary
    public DataSource dynamicDataSource(List<DynamicDataSourceProvider> providers){
        DynamicRoutingDataSource dynamicRoutingDataSource=new DynamicRoutingDataSource(providers);
        //设置多数据源中的主数据源
        if (dataSourceProperty.getPrimary()!=null){
            dynamicRoutingDataSource.setPrimary(dataSourceProperty.getPrimary());
        }
        //严格匹配数据源,默认false. true未匹配到指定数据源时抛异常,false使用默认数据源
        dynamicRoutingDataSource.setStrict(false);
        return dynamicRoutingDataSource;
    }
}
