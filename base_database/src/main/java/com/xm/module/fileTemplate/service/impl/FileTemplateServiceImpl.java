package com.xm.module.fileTemplate.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.module.config.domain.entity.TcConfig;
import com.xm.module.fileTemplate.config.FileTemplateConfig;
import com.xm.module.fileTemplate.domain.dto.FileTemplateSaveDto;
import com.xm.module.fileTemplate.domain.vo.FileTemplateDataVo;
import com.xm.module.fileTemplate.service.FileTemplateService;
import com.xm.util.config.ConfigUtil;
import com.xm.util.file.save.FileListJsonUtil;
import com.xm.util.id.SnowIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileTemplateServiceImpl implements FileTemplateService {

    @Override
    public List<FileTemplateDataVo> getDataByCode(String code) {
        TcConfig config = ConfigUtil.getConfig(FileTemplateConfig.groupCode, code);
        if (config==null){
            return new ArrayList<>();
        }else {
            return JSONUtil.toList(JSONUtil.parseArray(config.getConfigValue()), FileTemplateDataVo.class);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateData(FileTemplateSaveDto saveDto) {
        String code = saveDto.getCode();
        List<FileTemplateDataVo> dataList = saveDto.getDataList();
        List<FileTemplateDataVo> deleteList = saveDto.getDeleteList();
        Map<String, String> tempIdAndFileIdMap = saveDto.getTempIdAndFileIdMap();
        if (StrUtil.isBlank(code)){
            throw new CommonException("编码为空，无法获取文件模板");
        }
        if (CollectionUtil.isNotEmpty(deleteList)){
            for (FileTemplateDataVo deleteVo:deleteList){
                FileListJsonUtil.executeDeleteAllFile(deleteVo.getTemplateFile());
            }
        }
        List<FileTemplateDataVo> oldDataList = getDataByCode(code);
        Map<String, FileTemplateDataVo> oldDataIdMapping = oldDataList.stream().collect(Collectors.toMap(FileTemplateDataVo::getId, Function.identity()));
        for (FileTemplateDataVo dataVo:dataList){
            String templateFile = dataVo.getTemplateFile();
            if (StrUtil.isBlank(dataVo.getId())){
                dataVo.setId(SnowIdUtil.getSnowId());
                dataVo.setTemplateFile(FileListJsonUtil.executeMappingFileWithOldFileListJson(templateFile,null,tempIdAndFileIdMap));
            }else {
                FileTemplateDataVo oldData = oldDataIdMapping.get(dataVo.getId());
                if (oldData==null){
                    dataVo.setTemplateFile(FileListJsonUtil.executeMappingFileWithOldFileListJson(templateFile,null,tempIdAndFileIdMap));
                }else {
                    dataVo.setTemplateFile(FileListJsonUtil.executeMappingFileWithOldFileListJson(templateFile,oldData.getTemplateFile(),tempIdAndFileIdMap));
                }
            }
        }
        ConfigUtil.saveConfig(FileTemplateConfig.groupCode,code, JSONUtil.toJsonStr(dataList));
        return "操作成功";
    }
}
