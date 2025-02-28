package com.xm.file.domain.params;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UploadChunkParams {
    @NotNull(message = "块定位不能为空")
    private Integer chunkNumber;
    @NotNull(message = "块大小不能为空")
    private Integer chunkSize;
    @NotBlank(message = "文件路径不能为空")
    private String filePath;
    @NotBlank(message = "文件名称不能为空")
    private String fileName;
    @NotBlank(message = "uploadId不能为空")
    private String uploadId;
    @NotBlank(message = "md5不能为空")
    private String md5;
}
