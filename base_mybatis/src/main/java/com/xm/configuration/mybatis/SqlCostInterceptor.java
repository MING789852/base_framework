package com.xm.configuration.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class,Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class,Object.class})
})
@Slf4j
public class SqlCostInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            long timeCost = endTime - startTime;
//            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
//            String sql = statementHandler.getBoundSql().getSql();
            Object[] args = invocation.getArgs();
            if (args!=null){
                MappedStatement mappedStatement=(MappedStatement) args[0];
                Object parameter = null;
                if (args.length>=2){
                    parameter=args[1];
                }
                log.debug("[{}ms]{},{}",timeCost,mappedStatement.getId(),parameter);
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {}
}