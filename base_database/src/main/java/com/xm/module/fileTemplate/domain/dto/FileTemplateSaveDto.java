package com.xm.module.fileTemplate.domain.dto;

import com.xm.module.fileTemplate.domain.vo.FileTemplateDataVo;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FileTemplateSaveDto {
    private String code;
    private List<FileTemplateDataVo> dataList;
    private List<FileTemplateDataVo> deleteList;

    //文件上传对应表
    private Map<String,String> tempIdAndFileIdMap;
}
