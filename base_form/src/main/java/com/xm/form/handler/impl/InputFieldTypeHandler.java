package com.xm.form.handler.impl;

import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.form.domain.entity.TcFormDetailModel;
import com.xm.form.domain.entity.TcFormDynamicField;
import com.xm.form.enums.FormFieldTypeEnum;
import com.xm.form.enums.FormModelFileUploadType;
import com.xm.form.handler.ExternalFieldTypeHandler;
import com.xm.form.service.TcFormDynamicFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class InputFieldTypeHandler implements ExternalFieldTypeHandler {

    private final TcFormDynamicFieldService dynamicFieldService;

    @Override
    public String getFieldType() {
        return FormFieldTypeEnum.INPUT.getValue();
    }

    @Override
    public void saveData(TcFormDetailModel detailModel, String formInsId,
                         String refId, String refType,
                         Map<String, Object> data, FormModelFileUploadType formModelFileUploadType,
                         Map<String, UploadFileWithStream> uploadFileWithStreamMap,Map<String,String> tempIdAndFileIdMap) {
        if (detailModel==null){
            return;
        }
        String formDetailId = detailModel.getId();
        if (StrUtil.isBlank(formDetailId)){
            return;
        }
        if (StrUtil.isBlank(formInsId)){
            return;
        }
        String fieldCode = detailModel.getFieldCode();
        Object object = data.get(fieldCode);
        //判断是否必填
        if (detailModel.getRequired()){
            if (object==null){
                throw new CommonException(StrUtil.format("{}不能为空",detailModel.getFieldName()));
            }
        }
        if (object instanceof String){
            String text=object.toString();
            TcFormDynamicField dynamicField=new TcFormDynamicField();
            dynamicField.setFieldText(text);
            dynamicField.setRefType(refType);
            dynamicField.setRefId(refId);
            dynamicField.setFormDetailId(formDetailId);
            dynamicField.setFormInsId(formInsId);
            dynamicFieldService.saveDynamicField(dynamicField);
        }
    }

    @Override
    public void getData(TcFormDetailModel detailModel,String formInsId, String refId, String refType, Map<String, Object> data) {
        if (detailModel==null){
            return;
        }
        String formDetailId = detailModel.getId();
        if (StrUtil.isBlank(formDetailId)){
            return;
        }
        if (StrUtil.isBlank(formInsId)){
            return;
        }
        TcFormDynamicField dynamicField = dynamicFieldService.getDynamicField(formInsId, formDetailId);
        if (dynamicField==null){
            return;
        }
        String fieldText = dynamicField.getFieldText();
        if (StrUtil.isNotBlank(fieldText)){
            data.put(detailModel.getFieldCode(),fieldText);
        }
    }

    @Override
    public void deleteData(TcFormDetailModel detailModel, String formInsId, String refId, String refType) {

    }
}
