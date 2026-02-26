package com.xm.util.sql;

import cn.hutool.core.bean.BeanUtil;
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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.emuns.DbTypeEnum;
import com.xm.module.core.mapper.CoreBaseMapper;
import com.xm.util.bean.SpringBeanUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;


public class SqlGenerateUtil {

    public static long getOffset(long pageNum,long pageSize){
        if (pageNum<1){
            pageNum=1;
        }
        return (pageNum-1)*pageSize;
    }

    public static <T> Page<T> getPageData(String sql, Page<T> page,Wrapper<T> queryWrapper,Class<T> tClass, DbTypeEnum dbTypeEnum){
        CoreBaseMapper baseMapper = SpringBeanUtil.getBeanByClass(CoreBaseMapper.class);
        PageSqlResult pageSqlResult = SqlGenerateUtil.getPageSql(sql, page.getCurrent(), page.getSize(), queryWrapper, dbTypeEnum);
        String countSql = pageSqlResult.getCountSql();
        String pageSql = pageSqlResult.getPageSql();
        Long count = baseMapper.selectCountByOnlySql(countSql,queryWrapper);
        List<Map<String, Object>> mapList = baseMapper.selectMapListByOnlySql(pageSql,queryWrapper);
        page.setRecords(BeanUtil.copyToList(mapList, tClass));
        page.setTotal(count);
        return page;
    }

    public static <T> PageSqlResult  getPageSql(String sql, long pageNum, long pageSize, Wrapper<T> wrapper, DbTypeEnum dbTypeEnum){
        PageSqlResult pageSqlResult=new PageSqlResult();

        if (Objects.requireNonNull(dbTypeEnum) == DbTypeEnum.sqlserver) {
            SQLStatement sqlStatement = SQLUtils.parseSingleStatement(sql, DbType.sqlserver, false);
            if (sqlStatement instanceof SQLSelectStatement) {
                SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
                SQLSelect select = sqlSelectStatement.getSelect();
                pageSqlResult.setPageSql(getSqlSeverPageSql(select, pageNum, pageSize, wrapper));
                pageSqlResult.setCountSql(getCountSql(select, wrapper));
            }
        }

        return pageSqlResult;
    }


    public static <T> String getSql(String sql,Wrapper<T> wrapper, DbTypeEnum dbTypeEnum){
        String resultSql = "";
        if (Objects.requireNonNull(dbTypeEnum) == DbTypeEnum.sqlserver) {
            SQLStatement sqlStatement = SQLUtils.parseSingleStatement(sql, DbType.sqlserver, false);
            if (sqlStatement instanceof SQLSelectStatement) {
                SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
                SQLSelect select = sqlSelectStatement.getSelect();
                resultSql = getSqlServerSql(select, wrapper);
            }
        }
        return  resultSql;
    }

    private  static <T> String getCountSql(SQLSelect select,Wrapper<T> wrapper){
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

    private static <T> String getSqlServerSql(SQLSelect select,Wrapper<T> wrapper){
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

    private static <T> String getSqlSeverPageSql(SQLSelect select,long pageNum,long pageSize,Wrapper<T> wrapper){
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
