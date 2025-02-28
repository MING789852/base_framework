package com.xm.file.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class TcFileChunk implements Serializable {
    /**
    * 主键
    */
    @Size(max = 32,message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
    * 文件MD5
    */
    @Size(max = 255,message = "文件MD5最大长度要小于 255")
    private String md5;

    /**
    * 上传唯一ID
    */
    @Size(max = 255,message = "上传唯一ID最大长度要小于 255")
    private String uploadId;

    /**
    * 文件名
    */
    @Size(max = 255,message = "文件名最大长度要小于 255")
    private String fileName;

    /**
    * 文件位置
    */
    @Size(max = 255,message = "文件位置最大长度要小于 255")
    private String filePath;

    /**
    * 分块定位
    */
    private Integer chunkNumber;

    /**
    * 分块大小
    */
    private Integer chunkSize;

    /**
    * 分块其他信息1
    */
    @Size(max = 255,message = "分块其他信息1最大长度要小于 255")
    private String other1;

    /**
    * 分块其他信息2,minio用于存储etag
    */
    @Size(max = 255,message = "分块其他信息2,minio用于存储etag最大长度要小于 255")
    private String other2;

    /**
    * 分块其他信息3
    */
    @Size(max = 255,message = "分块其他信息3最大长度要小于 255")
    private String other3;

    /**
    * 创建日期
    */
    private Date createDate;

    private static final long serialVersionUID = 1L;
}