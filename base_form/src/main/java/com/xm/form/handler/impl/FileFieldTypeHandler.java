package com.xm.form.handler.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.form.domain.entity.TcFormDetailModel;
import com.xm.form.domain.entity.TcFormFile;
import com.xm.form.enums.FormFieldTypeEnum;
import com.xm.form.enums.FormModelFileUploadType;
import com.xm.form.handler.ExternalFieldTypeHandler;
import com.xm.form.service.TcFormFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileFieldTypeHandler implements ExternalFieldTypeHandler {

    private final TcFormFileService formFileService;

    @Override
    public String getFieldType() {
        return FormFieldTypeEnum.FILE.getValue();
    }

    @Override
    public void saveData(TcFormDetailModel detailModel, String formInsId,
                         String refId, String refType,
                         Map<String, Object> data, FormModelFileUploadType formModelFileUploadType,
                         Map<String, UploadFileWithStream> uploadFileWithStreamMap,Map<String,String> tempIdAndFileIdMap) {
        String fieldCode = detailModel.getFieldCode();
        Object object = data.get(fieldCode);
        //判断是否必填
        if (detailModel.getRequired()){
            if (object==null){
                throw new CommonException(StrUtil.format("{}不能为空",detailModel.getFieldName()));
            }
        }
        if (object instanceof List){
            List<?> list= (List<?>) object;
            if (detailModel.getRequired()){
                if (CollectionUtil.isEmpty(list)){
                    throw new CommonException(StrUtil.format("{}不能为空",detailModel.getFieldName()));
                }
            }
            List<TcFormFile> formFileList = BeanUtil.copyToList(list, TcFormFile.class);
            if (FormModelFileUploadType.TEMP_UPLOAD.equals(formModelFileUploadType)){
                formFileService.saveOrUpdate(refType,refId,fieldCode,uploadFileWithStreamMap,formFileList);
            }
            if (FormModelFileUploadType.TEMP_MAPPING.equals(formModelFileUploadType)){
                formFileService.saveOrUpdateWithExistsFileMap(refType,refId,fieldCode,tempIdAndFileIdMap,formFileList);
            }
        }
    }

    @Override
    public void getData(TcFormDetailModel detailModel, String formInsId, String refId, String refType, Map<String, Object> data) {
        String fieldCode = detailModel.getFieldCode();
        List<TcFormFile> formFileList = formFileService.getFormFileList(refType, refId, fieldCode);
        if (CollectionUtil.isNotEmpty(formFileList)){
            data.put(detailModel.getFieldCode(),formFileList);
        }
    }

    @Override
    public void deleteData(TcFormDetailModel detailModel, String formInsId, String refId, String refType) {
        String fieldCode = detailModel.getFieldCode();
        formFileService.delete(refType,refId,fieldCode);
    }
}
