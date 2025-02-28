package com.xm.form.service;

import com.xm.module.core.service.BaseService;
import com.xm.form.domain.entity.TcFormMainInstance;
import com.xm.form.mapper.TcFormMainInstanceMapper;

import java.util.List;

public interface TcFormMainInstanceService extends BaseService<TcFormMainInstanceMapper, TcFormMainInstance> {
    @Override
    TcFormMainInstanceMapper getMapper();

    @Override
    String deleteData(List<TcFormMainInstance> list);

    @Override
    String saveOrUpdateData(List<TcFormMainInstance> list);

    TcFormMainInstance createInstance(String formInsCode,String formInsName,String mainModelId,String refId,String refType);

    TcFormMainInstance getInstanceById(String id);

    void deleteDataById(List<String> idList);

    boolean existById(String id);
}
