package com.xm.module.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.module.core.params.QueryData;
import com.xm.module.core.service.BaseService;
import com.xm.module.dict.domain.dto.SaveOrUpdateDictDto;
import com.xm.module.dict.domain.entity.TcDict;
import com.xm.module.dict.domain.query.DictQuery;
import com.xm.module.dict.mapper.TcDictMapper;

import java.util.List;
import java.util.Map;

public interface TcDictService extends BaseService<TcDictMapper, TcDict> {

    Page<TcDict> selectByPage(QueryData<TcDict> queryData);

    /**
     * 正向字典(带子项)
     */
    Map<String, String> getDictByGroupKey(String groupKey);

    /**
     * 反向字典(带子项)
     */
    Map<String, String> getReverseDictByGroupKey(String groupKey);

    /**
     * 查询groupKey下所有字典，不区分子项父项
     */
    List<TcDict> getDictListByGroupKey(String groupKey);

    /**
     * 前端使用的字典
     */
    Map<String,Object> getDictMappingByQuery(List<DictQuery> dictQueryList);

    /**
     * 获取反向字典
     */
    Map<String,Map<String,String>> getReverseDictStrMappingByQuery(List<DictQuery> dictQueryList);

    Map<String,Map<String,String>> getDictStrMappingByQuery(List<DictQuery> dictQueryList);

    String deleteData(List<TcDict> list);

    String saveOrUpdateData(SaveOrUpdateDictDto dictDto);
}
