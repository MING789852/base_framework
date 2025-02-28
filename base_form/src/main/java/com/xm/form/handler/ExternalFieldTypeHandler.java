package com.xm.form.handler;

import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.form.domain.entity.TcFormDetailModel;
import com.xm.form.enums.FormModelFileUploadType;

import java.util.Map;

public interface ExternalFieldTypeHandler {

    String getFieldType();

    void saveData(TcFormDetailModel detailModel, String formInsId,
                  String refId, String refType,
                  Map<String, Object> data, FormModelFileUploadType formModelFileUploadType,
                  Map<String, UploadFileWithStream> uploadFileWithStreamMap,Map<String,String> tempIdAndFileIdMap);

    void getData(TcFormDetailModel detailModel,String formInsId, String refId, String refType, Map<String, Object> data);

    void deleteData(TcFormDetailModel detailModel,String formInsId, String refId, String refType);
}
