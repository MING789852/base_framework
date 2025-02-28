package com.xm.status.service;

import com.xm.module.core.service.BaseService;
import com.xm.status.domain.entity.TcStatusMainInstance;
import com.xm.status.mapper.TcStatusMainInstanceMapper;

public interface TcStatusMainInstanceService extends BaseService<TcStatusMainInstanceMapper, TcStatusMainInstance> {

    @Override
    TcStatusMainInstanceMapper getMapper();

    boolean existInstanceByMainModelId(String mainModelId);

    TcStatusMainInstance getInstanceById(String id);

    TcStatusMainInstance createInstance(String statusInsCode,String statusInsName,String mainModelId,String refId,String refType);
}
