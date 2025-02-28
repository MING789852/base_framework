package com.xm.form.handler.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.form.domain.entity.TcFormDetailModel;
import com.xm.form.domain.entity.TcFormLabel;
import com.xm.form.enums.FormFieldTypeEnum;
import com.xm.form.enums.FormModelFileUploadType;
import com.xm.form.handler.ExternalFieldTypeHandler;
import com.xm.form.service.TcFormLabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LabelFieldTypeHandler implements ExternalFieldTypeHandler {

    private final TcFormLabelService formLabelService;

    @Override
    public String getFieldType() {
        return FormFieldTypeEnum.LABEL.getValue();
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
            List<TcFormLabel> formLabelList = BeanUtil.copyToList(list, TcFormLabel.class);
            formLabelService.saveOrUpdate(refType,refId,formLabelList);
        }
    }

    @Override
    public void getData(TcFormDetailModel detailModel, String formInsId, String refId, String refType, Map<String, Object> data) {
        List<TcFormLabel> formLabelList = formLabelService.getFormLabelList(refType, refId);
        if (CollectionUtil.isNotEmpty(formLabelList)){
            data.put(detailModel.getFieldCode(),formLabelList);
        }
    }

    @Override
    public void deleteData(TcFormDetailModel detailModel, String formInsId, String refId, String refType) {
        formLabelService.delete(refType,refId);
    }
}
