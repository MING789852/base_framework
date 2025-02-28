package com.xm.status.service;

import com.xm.module.core.service.BaseService;
import com.xm.status.domain.entity.TcStatusDetailModel;
import com.xm.status.mapper.TcStatusDetailModelMapper;

import java.util.List;
import java.util.Map;

public interface TcStatusDetailModelService extends BaseService<TcStatusDetailModelMapper, TcStatusDetailModel> {
    @Override
    TcStatusDetailModelMapper getMapper();

    @Override
    String deleteData(List<TcStatusDetailModel> list);

    @Override
    String saveOrUpdateData(List<TcStatusDetailModel> list);

    Map<String, Object> getDictMapping();

    Map<String,String> getDetailModelMappingByMainModelId(String mainModelId);

    List<TcStatusDetailModel> getDetailModelListByMainModelId(String mainModelId);

    TcStatusDetailModel getStatusDetailModelById(String id);

    void deleteByMainModelId(String mainModelId);
}
