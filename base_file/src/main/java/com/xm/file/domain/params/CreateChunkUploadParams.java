package com.xm.file.domain.params;

import lombok.Data;

@Data
public class CreateChunkUploadParams {
    private String fileName;
    private Long fileSize;
    private String md5;
}
