package com.xm.file.domain.params;

import lombok.Data;

@Data
public class MergeChunkParams {
    private String uploadId;
    private String fileName;
    private String filePath;
    private String md5;
}
