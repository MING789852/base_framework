package com.xm.form.service;


import com.xm.form.domain.entity.TcFormLabel;

import java.util.List;
import java.util.Map;

public interface TcFormLabelService {
    void saveOrUpdate(String moduleName,String refId, List<TcFormLabel> labelList);

    void delete(String moduleName,String refId);

    List<TcFormLabel> getFormLabelList(String moduleName, String refId);

    List<TcFormLabel> getAllFormLabelList();

    Map<String,String> getAllLabelNameMapping();
}
