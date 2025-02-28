package com.xm.module.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.module.core.params.QueryData;

import java.util.List;

public interface BaseService<M extends BaseMapper<T>,T> {
    M getMapper();

    default List<T> selectByList(QueryData<T> queryData){
        QueryWrapper<T> queryWrapper = queryData.generateQueryWrapper();
        return getMapper().selectList(queryWrapper);
    }

    default Page<T> selectByPage(QueryData<T> queryData){
        Page<T> page=new Page<>(queryData.getCurrent(), queryData.getSize());
        QueryWrapper<T> queryWrapper = queryData.generateQueryWrapper();
        page = getMapper().selectPage(page, queryWrapper);
        return page;
    }

    default String deleteData(List<T> list){
        return null;
    }

    default String saveOrUpdateData(List<T> list){
        return null;
    }

}
