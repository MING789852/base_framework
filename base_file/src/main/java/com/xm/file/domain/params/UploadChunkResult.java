package com.xm.file.domain.params;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UploadChunkResult extends UploadChunkParams{
    //数据库ID
    private String chunkId;
    //用于记录minio分块
    private String etag;
}
