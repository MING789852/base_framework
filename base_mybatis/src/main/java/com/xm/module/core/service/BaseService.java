package com.xm.module.core.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.core.params.ColumnProps;
import com.xm.module.core.params.QueryData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface BaseService<M extends BaseMapper<T>,T> {
    M getMapper();

    default List<ColumnProps> getColumnPropsList(){
        return new ArrayList<>();
    }

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
        if (CollectionUtil.isNotEmpty(list)){
            List<String> deleteIdList = new ArrayList<>();
            for (T item:list){
                Object fieldValue = ReflectUtil.getFieldValue(item, "id");
                if (fieldValue != null){
                    deleteIdList.add(fieldValue.toString());
                }
            }
            if (CollectionUtil.isNotEmpty(deleteIdList)){
                getMapper().deleteByIds(deleteIdList);
            }
        }
        return "操作成功";
    }

    default String saveOrUpdateData(List<T> list){
        return null;
    }

}
