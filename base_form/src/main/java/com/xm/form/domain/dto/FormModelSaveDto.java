package com.xm.form.domain.dto;

import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.form.enums.FormModelFileUploadType;
import lombok.Data;

import java.util.Map;

@Data
public class FormModelSaveDto {
    private String refId;
    private String refType;
    private String mainModelId;
    private String formInsId;
    private String formInsName;
    private String formInsCode;
    private Map<String, Object> data;
    private Map<String, UploadFileWithStream> uploadFileWithStreamMap;
    private Map<String,String> tempIdAndFileIdMap;
    private FormModelFileUploadType uploadType = FormModelFileUploadType.TEMP_UPLOAD;
}
