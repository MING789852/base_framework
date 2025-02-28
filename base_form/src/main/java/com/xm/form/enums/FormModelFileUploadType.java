package com.xm.form.enums;

import lombok.Getter;

@Getter
public enum FormModelFileUploadType {
    //Map<String, UploadFileWithStream> uploadFileWithStreamMap必填
    TEMP_UPLOAD(1,"临时文件上传"),
    //Map<String,String> tempIdAndFileIdMap必填
    TEMP_MAPPING(2,"关联已上传文件");

    private final Integer value;
    private final String label;
    FormModelFileUploadType(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
