package com.xm.module.core.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CoreBaseMapper {
    /**
     * 极度危险的操作，请调用前校验sql注入
     * @param page
     * @param dynamicSql
     * @param ew
     * @return
     */
    Page<Map<String, Object>> selectPageBySql(Page<Map<String, Object>> page,
                                          @Param("dynamicSql") String dynamicSql,
                                          @Param("ew") Wrapper<?> ew);
    /**
     * 极度危险的操作，请调用前校验sql注入
     * @param dynamicSql
     * @param ew
     * @return
     */
    List<Map<String, Object>> selectListBySql(@Param("dynamicSql") String dynamicSql,
                                              @Param("ew") Wrapper<?> ew);

    /**
     * 极度危险的操作，请调用前校验sql注入
     * @param dynamicSql
     * @return
     */
    List<Map<String, Object>> selectMapListByOnlySql(@Param("dynamicSql") String dynamicSql,@Param("ew") Wrapper<?> ew);

    /**
     * 极度危险的操作，请调用前校验sql注入
     * @param dynamicSql
     * @return
     */
    Long selectCountByOnlySql(@Param("dynamicSql") String dynamicSql,@Param("ew") Wrapper<?> ew);
}
