package com.xm.file.domain.dto;

import lombok.Data;

import java.io.InputStream;

@Data
public class UploadFileWithStream {
    private String fileName;
    private Long fileSize;
    private InputStream inputStream;
}
