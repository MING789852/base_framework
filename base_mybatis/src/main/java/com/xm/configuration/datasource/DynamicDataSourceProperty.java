package com.xm.configuration.datasource;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties(prefix = "spring.datasource")
@Component
@Data
public class DynamicDataSourceProperty {
    private String primary;
    private Map<String, DataSourceProperty> dataSourcePropertyMap;
}
