package com.xm.status.service;

import com.xm.module.core.service.BaseService;
import com.xm.status.domain.dto.TcStatusDetailInstanceDto;
import com.xm.status.domain.entity.TcStatusDetailInstance;
import com.xm.status.mapper.TcStatusDetailInstanceMapper;

import java.util.List;

public interface TcStatusDetailInstanceService extends BaseService<TcStatusDetailInstanceMapper,TcStatusDetailInstance> {

    @Override
    TcStatusDetailInstanceMapper getMapper();

    @Override
    String saveOrUpdateData(List<TcStatusDetailInstance> list);

    void saveDtoData(String statusMainInstanceId,List<TcStatusDetailInstanceDto> detailInstanceDtoList);

    boolean existsStatusDetailInstance(String statusMainInstanceId, String statusDetailModelId);

    List<TcStatusDetailInstance> getStatusDetailInstanceByMainInsId(String mainInsId);
}
