package com.xm.configuration.datasource;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    private final DynamicDataSourceProperty dataSourceProperty;

    @Bean
    public ServletRegistrationBean<StatViewServlet> statViewServlet() {
        ServletRegistrationBean<StatViewServlet> servletRegistrationBean =
                new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");

        // 设置监控页面的配置参数
//        servletRegistrationBean.addInitParameter("allow", "127.0.0.1"); // 允许访问的 IP
//        servletRegistrationBean.addInitParameter("deny", "127.0.0.2"); // 拒绝访问的 IP
        servletRegistrationBean.addInitParameter("loginUsername", "admin"); // 登录用户名
        servletRegistrationBean.addInitParameter("loginPassword", "admin"); // 登录密码
        servletRegistrationBean.addInitParameter("resetEnable", "false"); // 是否允许重置数据

        return servletRegistrationBean;
    }

    // 配置 Druid 过滤器
    @Bean
    public FilterRegistrationBean<WebStatFilter> statFilter() {
        FilterRegistrationBean<WebStatFilter> filterRegistrationBean =
                new FilterRegistrationBean<>(new WebStatFilter());

        // 配置过滤规则
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");

        return filterRegistrationBean;
    }

    private void settingDruidDataSource(DruidDataSource druidDataSource) {
        druidDataSource.setInitialSize(5);
        druidDataSource.setMinIdle(1);
        druidDataSource.setMaxActive(30);
        // 配置获取连接等待超时的时间
        druidDataSource.setMaxWait(60000);
        // 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        druidDataSource.setTimeBetweenEvictionRunsMillis(30000);
        //检测间隔(1分钟)与最小空闲时间(3分钟)保持1:3关系
        // 配置一个连接在池中最小生存的时间，单位是毫秒
        // 连接保持空闲而不被驱逐的最小时间
        druidDataSource.setMinEvictableIdleTimeMillis(30000*3);
        // 连接池中的minIdle数量以内的连接，空闲时间超过minEvictableIdleTimeMillis，则会执行keepAlive操作。实际项目中建议配置成true
        druidDataSource.setKeepAlive(true);
        // 检测连接是否可用
        druidDataSource.setValidationQuery("SELECT 1");
        druidDataSource.setTestWhileIdle(true);
        //申请连接时执行validationQuery检测连接是否有效，配置为true会降低性能
        druidDataSource.setTestOnBorrow(false);
        //归还连接时执行validationQuery检测连接是否有效，配置为true会降低性能
        druidDataSource.setTestOnReturn(false);
//        //即相同逻辑的SQL可以复用一个游标，这样可以减少创建游标的数量
//        druidDataSource.setPoolPreparedStatements(true);
        try {
            druidDataSource.setFilters("stat");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
//        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
    }

    public DataSource createDruidDataSource(String name,DataSourceProperty dataSourceProperty){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setName(name);
        druidDataSource.setUrl(dataSourceProperty.getUrl());
        druidDataSource.setDriverClassName(dataSourceProperty.getDriverClassName());
        druidDataSource.setUsername(dataSourceProperty.getUsername());
        druidDataSource.setPassword(dataSourceProperty.getPassword());
        settingDruidDataSource(druidDataSource);
        return druidDataSource;
    }

    @Bean
    public Map<String, DataSource> dataSourceMap(){
        Map<String,DataSource> dataSourceMap=new HashMap<>();
        if (CollectionUtil.isNotEmpty(dataSourceProperty.getDataSourcePropertyMap())){
            for (Map.Entry<String,DataSourceProperty> dataSourceEntry:dataSourceProperty.getDataSourcePropertyMap().entrySet()){
                DataSource druidDataSource = createDruidDataSource(dataSourceEntry.getKey(), dataSourceEntry.getValue());
                dataSourceMap.put(dataSourceEntry.getKey(),druidDataSource);
            }
        }
        return dataSourceMap;
    }

    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("dynamicDataSource") DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }

//    @Bean
//    public PlatformTransactionManager transactionManager(){
//        UserTransactionManager userTransactionManager=new UserTransactionManager();
//        //close()时是否强制终止事务
//        userTransactionManager.setForceShutdown(true);
//
//        UserTransaction userTransaction=new UserTransactionImp();
//        JtaTransactionManager jtaTransactionManager=new JtaTransactionManager();
//        jtaTransactionManager.setTransactionManager(userTransactionManager);
//        jtaTransactionManager.setUserTransaction(userTransaction);
//        jtaTransactionManager.setAllowCustomIsolationLevels(true);
//        return jtaTransactionManager;
//    }
}
