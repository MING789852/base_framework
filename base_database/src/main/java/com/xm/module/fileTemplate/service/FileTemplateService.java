package com.xm.module.fileTemplate.service;

import com.xm.module.fileTemplate.domain.dto.FileTemplateSaveDto;
import com.xm.module.fileTemplate.domain.vo.FileTemplateDataVo;

import java.util.List;

public interface FileTemplateService {

    String saveOrUpdateData(FileTemplateSaveDto saveDto);

    List<FileTemplateDataVo> getDataByCode(String code);
}
