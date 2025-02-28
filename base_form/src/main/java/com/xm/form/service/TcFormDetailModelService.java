package com.xm.form.service;

import com.xm.module.core.service.BaseService;
import com.xm.form.domain.entity.TcFormDetailModel;
import com.xm.form.mapper.TcFormDetailModelMapper;

import java.util.List;
import java.util.Map;

public interface TcFormDetailModelService extends BaseService<TcFormDetailModelMapper, TcFormDetailModel> {
    @Override
    TcFormDetailModelMapper getMapper();

    @Override
    String deleteData(List<TcFormDetailModel> list);

    @Override
    String saveOrUpdateData(List<TcFormDetailModel> list);

    void deleteByMainIdList(List<String> mainIdList);

    List<TcFormDetailModel> getShowDetailDataListByMainModelId(String mainId);

    Map<String,Object> getDictMapping();
}
