package com.xm.form.service;

import com.xm.module.core.service.BaseService;
import com.xm.form.domain.dto.FormModelDeleteDto;
import com.xm.form.domain.dto.FormModelSaveDto;
import com.xm.form.domain.entity.TcFormDetailModel;
import com.xm.form.domain.entity.TcFormMainInstance;
import com.xm.form.domain.entity.TcFormMainModel;
import com.xm.form.domain.query.FormModelQuery;
import com.xm.form.domain.vo.FormModelResult;
import com.xm.form.mapper.TcFormMainModelMapper;

import java.util.List;
import java.util.Map;

public interface TcFormMainModelService extends BaseService<TcFormMainModelMapper, TcFormMainModel> {
    @Override
    TcFormMainModelMapper getMapper();

    @Override
    String deleteData(List<TcFormMainModel> list);

    @Override
    String saveOrUpdateData(List<TcFormMainModel> list);

    Map<String,String> getFormMainModelIdAndNameMapping();

    FormModelResult getFormModelResultById(FormModelQuery query);

    TcFormMainInstance saveFormModelData(FormModelSaveDto saveDto);

    void deleteFormModelData(FormModelDeleteDto deleteDto);

    TcFormMainModel getModelByCode(String code);

    TcFormMainModel getModelById(String id);

    List<TcFormDetailModel> getDetailModelByCode(String code);

    List<TcFormDetailModel> getDetailModelByMainId(String mainId);
}
