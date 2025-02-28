package com.xm.file.domain.params;

import lombok.Data;

@Data
public class UploadFileParams {
    private String fileName;
    private Long fileSize;
}
