package com.xm.util.sql;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.xm.advice.exception.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

@Slf4j
@Component
public class MyBatisUtil {

    private static Map<String, DataSource> dataSourceMap;

    private static final ConcurrentMap<String, SqlSessionFactory> sqlSessionFactoryMap = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("dataSourceMap")
    public  void setDataSourceMap(Map<String, DataSource> dataSourceMap) {
        MyBatisUtil.dataSourceMap = dataSourceMap;
    }

    @PreDestroy
    public static void destroy() {
        sqlSessionFactoryMap.clear();
    }

    private static Resource[] resolveMapperLocations() {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<String> mapperLocations = new ArrayList<>();
        mapperLocations.add("classpath*:/mapper/**/*.xml");
        List<Resource> resources = new ArrayList<>();
        for (String mapperLocation : mapperLocations) {
            try {
                Resource[] mappers = resourceResolver.getResources(mapperLocation);
                resources.addAll(Arrays.asList(mappers));
            } catch (IOException e) {
                // ignore
            }
        }
        return resources.toArray(new Resource[0]);
    }

    public static  SqlSessionFactory getSqlSessionFactoryByDataSourceName(String dataSourceName)  {
        // 第一重检查：如果存在，直接返回
        if (sqlSessionFactoryMap.containsKey(dataSourceName)) {
            return sqlSessionFactoryMap.get(dataSourceName);
        }
        // 同步块，确保只有一个线程进入创建流程
        synchronized (MyBatisUtil.class) {
            // 第二重检查：进入同步块后再次检查，防止重复创建
            if (sqlSessionFactoryMap.containsKey(dataSourceName)) {
                return sqlSessionFactoryMap.get(dataSourceName);
            }
            try {
                MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
                sqlSessionFactoryBean.setDataSource(dataSourceMap.get(dataSourceName));
                sqlSessionFactoryBean.setMapperLocations(resolveMapperLocations());
                SqlSessionFactory factory = sqlSessionFactoryBean.getObject();
                sqlSessionFactoryMap.put(dataSourceName, factory);
                return factory;
            } catch (Exception e) {
                String msg=StrUtil.format("【getSqlSessionFactoryByDataSourceName】根据数据源名称[{}]获取SqlSessionFactory失败->{}", dataSourceName,ExceptionUtil.stacktraceToString(e));
                throw new CommonException(msg);
            }
        }
    }

    public static <T> Integer executeCursor(SqlSession sqlSession, Cursor<T> cursor, Consumer<T> consumer) {
        if (cursor == null || sqlSession == null || consumer == null) {
            // 在参数检查阶段如果发现空值，也应确保传入的sqlSession被关闭
            if (sqlSession != null) {
                sqlSession.close();
            }
            return 0;
        }

        int count = 0;
        try {
            for (T item : cursor) {
                consumer.accept(item);
                count++;
            }
            // 正常处理完成后，提交事务
            sqlSession.commit();
        } catch (Exception e) {
            String msg = StrUtil.format("【executeCursor】处理失败 {}",ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        } finally {
            // 关键：在finally块中确保资源关闭，顺序为先Cursor后SqlSession
            try {
                cursor.close(); // 直接关闭是安全的
                log.info("【executeCursor】游标已关闭");
            } catch (Exception e) {
                log.error("【executeCursor】关闭游标时发生异常: {}", e.getMessage());
            }
            try {
                sqlSession.close();
                log.info("【executeCursor】SqlSession已关闭");
            } catch (Exception e) {
                log.error("【executeCursor】关闭SqlSession时发生异常: {}", e.getMessage());
            }
        }
        return count;
    }
}
