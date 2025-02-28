package com.xm.status.domain.dto;

import com.xm.file.domain.dto.UploadFileWithStream;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StatusModelSaveDto {
    private String refId;
    private String refType;
    private String statusMainModelId;
    private String statusMainInstanceId;
    private String statusInsCode;
    private String statusInsName;
    private List<TcStatusDetailInstanceDto> detailInstanceDtoList;
    private Map<String, UploadFileWithStream> uploadFileWithStreamMap;
}
