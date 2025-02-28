package com.xm.module.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.module.core.params.QueryData;
import com.xm.module.dict.domain.entity.TcDictGroup;

import java.util.List;

public interface TcDictGroupService {

    List<TcDictGroup> selectByList(QueryData<TcDictGroup> queryData);

    Page<TcDictGroup> selectByPage(QueryData<TcDictGroup> queryData);

    String deleteData(List<TcDictGroup> list);

    String saveOrUpdateData(List<TcDictGroup> list);
}
