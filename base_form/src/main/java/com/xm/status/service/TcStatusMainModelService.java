package com.xm.status.service;

import com.xm.module.core.service.BaseService;
import com.xm.status.domain.dto.StatusChangeAddDto;
import com.xm.status.domain.dto.StatusModelSaveDto;
import com.xm.status.domain.dto.TcStatusDetailInstanceDto;
import com.xm.status.domain.entity.TcStatusMainModel;
import com.xm.status.domain.query.StatusModelQuery;
import com.xm.status.domain.vo.StatusModelDataResult;
import com.xm.status.domain.vo.StatusModelSaveOrUpdateResult;
import com.xm.status.mapper.TcStatusMainModelMapper;

import java.util.List;
import java.util.Map;

public interface TcStatusMainModelService extends BaseService<TcStatusMainModelMapper, TcStatusMainModel> {
    @Override
    TcStatusMainModelMapper getMapper();

    @Override
    String deleteData(List<TcStatusMainModel> list);

    @Override
    String saveOrUpdateData(List<TcStatusMainModel> list);

    Map<String,String> getStatusMainModelIdAndNameMapping();

    StatusModelSaveOrUpdateResult saveStatusModelData(StatusModelSaveDto saveOrUpdateDto);

    StatusModelDataResult getStatusModelResult(StatusModelQuery query);

    List<TcStatusDetailInstanceDto> statusModelChangeAdd(StatusChangeAddDto statusChangeAddDto);
}
