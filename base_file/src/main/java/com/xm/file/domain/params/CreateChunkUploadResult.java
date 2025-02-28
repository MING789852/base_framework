package com.xm.file.domain.params;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateChunkUploadResult extends CreateChunkUploadParams{
    //上传唯一ID（文件不存在有效）
    private String uploadId;
    //文件路径（文件不存在有效）
    private String filePath;
    //用于断点续传（文件不存在有效）
    private List<Integer> alreadyChunkNumberList;
    //文件是否存在
    private Boolean fileExists;
    //完整文件ID（文件存在有效）
    private String fileId;
}
