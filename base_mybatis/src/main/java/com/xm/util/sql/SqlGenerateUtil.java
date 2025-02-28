package com.xm.util.sql;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLWithSubqueryClause;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.emuns.DbTypeEnum;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class SqlGenerateUtil {

    public static <T> QueryWrapper<T> getAllFiledLikeQueryWrapper(String queryText, List<String> ignoreFields,Class<T> clazz) {
        QueryWrapper<T> queryWrapper=new QueryWrapper<>();
        if (StrUtil.isBlank(queryText)){
            return queryWrapper;
        }
        if (clazz==null){
            return queryWrapper;
        }
        if (CollectionUtil.isEmpty(ignoreFields)){
            ignoreFields=new ArrayList<>();
        }
        Field[] fields = ReflectUtil.getFields(clazz);
        for (Field field:fields){
            if (ignoreFields.contains(field.getName())){
                continue;
            }
            //驼峰转下划线
            queryWrapper.or().like(StrUtil.toUnderlineCase(field.getName()),queryText);
        }
        return queryWrapper;
    }


    public static long getOffset(long pageNum,long pageSize){
        if (pageNum<1){
            pageNum=1;
        }
        return (pageNum-1)*pageSize;
    }

    public static PageSqlResult getPageSql(String sql, long pageNum, long pageSize, Wrapper wrapper, DbTypeEnum dbTypeEnum){
        PageSqlResult pageSqlResult=new PageSqlResult();

        switch (dbTypeEnum){
            case sqlserver:
                SQLStatement sqlStatement = SQLUtils.parseSingleStatement(sql, DbType.sqlserver, false);
                if (sqlStatement instanceof SQLSelectStatement){
                    SQLSelectStatement sqlSelectStatement= (SQLSelectStatement) sqlStatement;
                    SQLSelect select = sqlSelectStatement.getSelect();
                    pageSqlResult.setPageSql(getSqlSeverPageSql(select,pageNum,pageSize,wrapper));
                    pageSqlResult.setCountSql(getCountSql(select,wrapper));
                }
        }

        return pageSqlResult;
    }


    public static String getSql(String sql,Wrapper wrapper, DbTypeEnum dbTypeEnum){
        String resultSql = "";
        switch (dbTypeEnum) {
            case sqlserver:
                SQLStatement sqlStatement = SQLUtils.parseSingleStatement(sql, DbType.sqlserver, false);
                if (sqlStatement instanceof SQLSelectStatement){
                    SQLSelectStatement sqlSelectStatement= (SQLSelectStatement) sqlStatement;
                    SQLSelect select = sqlSelectStatement.getSelect();
                    resultSql=getSqlServerSql(select,wrapper);
                }
        }
        return  resultSql;
    }


    private static String getCountSql(SQLSelect select,Wrapper wrapper){
        SQLSelectQueryBlock queryBlock = select.getQueryBlock();
        StringBuilder countSql=new StringBuilder();
        String notWithSql= queryBlock.toString();
        SQLOrderBy orderBy = queryBlock.getOrderBy();
        if (orderBy!=null){
            //去掉order by
            notWithSql = notWithSql.replace(orderBy.toString(), "");
        }
        String customSqlSegment = wrapper.getCustomSqlSegment();
        countSql.append("select count(1) from ").append("(\n")
                .append(notWithSql)
                .append(") as __count__ ").append(customSqlSegment);
        SQLWithSubqueryClause withSubQuery = select.getWithSubQuery();
        if (withSubQuery==null){
            return countSql.toString();
        }else {
            return withSubQuery + " \n" + countSql;
        }
    }

    private static String getSqlServerSql(SQLSelect select,Wrapper wrapper){
        SQLWithSubqueryClause withSubQuery = select.getWithSubQuery();
        SQLSelectQueryBlock queryBlock = select.getQueryBlock();
        String  notWithSql= queryBlock.toString();
        //获取order by
        SQLOrderBy orderBy = queryBlock.getOrderBy();
        if (orderBy!=null){
            //去掉order by
            notWithSql = notWithSql.replace(orderBy.toString(), "");
        }
        notWithSql = "select * from (\n" + notWithSql + "\n) as __temp__";
        //添加条件
        String customSqlSegment = wrapper.getCustomSqlSegment();
        notWithSql= notWithSql + " " +customSqlSegment;

        String sql;
        //添加with as语句
        if (withSubQuery!=null){
            sql = withSubQuery + "\n" + notWithSql;
        }else {
            sql = notWithSql;
        }
        //添加order by语句
        if (orderBy!=null){
            sql = sql + " " + orderBy;
        }
        return sql;
    }


    private static String getSqlSeverPageSql(SQLSelect select,long pageNum,long pageSize,Wrapper wrapper){
        SQLWithSubqueryClause withSubQuery = select.getWithSubQuery();
        SQLSelectQueryBlock queryBlock = select.getQueryBlock();
        String  notWithSql= queryBlock.toString();
        //获取order by
        SQLOrderBy orderBy = queryBlock.getOrderBy();
        if (orderBy!=null){
            //去掉order by
            notWithSql = notWithSql.replace(orderBy.toString(), "");
        }
        notWithSql = "select * from (\n" + notWithSql + "\n) as __page__";
        //添加条件
        String customSqlSegment = wrapper.getCustomSqlSegment();
        notWithSql= notWithSql + " " +customSqlSegment;
        //获取分页参数
        long offset = getOffset(pageNum, pageSize);
        String pageSql;

        if (orderBy==null){
            notWithSql= StrUtil.format("{}\n"+
                    "Order By 1\n" +
                    "OFFSET {} ROWS\n" +
                    "FETCH NEXT {} ROWS ONLY\n",notWithSql,offset,pageSize);
        }else {
            notWithSql= StrUtil.format("{}\n"+
                    "{}\n" +
                    "OFFSET {} ROWS\n" +
                    "FETCH NEXT {} ROWS ONLY\n",notWithSql,orderBy.toString(),offset,pageSize);
        }

        if (withSubQuery!=null){
            pageSql = withSubQuery + "\n" + notWithSql;
        }else {
            pageSql = notWithSql;
        }
        return pageSql;
    }

}
