package com.xm.util.sftp;

import lombok.Data;

import java.io.InputStream;

@Data
public class SftpUploadData {
    private InputStream inputStream;
    private String uploadDir;
    private String uploadFile;

    public SftpUploadData(InputStream inputStream, String uploadDir, String uploadFile) {
        this.inputStream = inputStream;
        this.uploadDir = uploadDir;
        this.uploadFile = uploadFile;
    }
}
