package com.xm.form.service;

import com.xm.module.core.service.BaseService;
import com.xm.form.domain.entity.TcFormDynamicField;
import com.xm.form.mapper.TcFormDynamicFieldMapper;

import java.util.List;

public interface TcFormDynamicFieldService extends BaseService<TcFormDynamicFieldMapper, TcFormDynamicField> {
    @Override
    TcFormDynamicFieldMapper getMapper();

    void saveDynamicField(TcFormDynamicField dynamicField);

    TcFormDynamicField getDynamicField(String formInsId,String formDetailId);

    void deleteByFormInsIdList(List<String> idList);
}
