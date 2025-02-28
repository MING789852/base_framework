package com.xm.flowable.service;

import com.xm.flowable.domain.dto.SaveProcessBindData;
import com.xm.flowable.domain.vo.ProcessBindData;

import java.util.List;
import java.util.Map;

public interface ProcessBindService {
    String saveProcessBindData(SaveProcessBindData saveProcessBindData);

    List<ProcessBindData>  getProcessBindData(String processBindDataCode);

    Map<String,Object> getDictMapping();

    String getProcessBindModel(String processBindDataCode,String value);
}
