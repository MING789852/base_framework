package com.xm.file.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 文件
 */
@Data
public class TcFile implements Serializable {
    /**
     * id
     */
    @Size(max = 32, message = "id最大长度要小于 32")
    @NotBlank(message = "id不能为空")
    private String id;

    /**
     * 0、不可用 1、可用
     */
    @Size(max = 10, message = "0、不可用 1、可用最大长度要小于 10")
    @NotBlank(message = "0、不可用 1、可用不能为空")
    private String status;

    /**
     * 文件名称（带后缀）
     */
    @Size(max = 50, message = "文件名称（带后缀）最大长度要小于 50")
    @NotBlank(message = "文件名称（带后缀）不能为空")
    private String originalFileName;

    /**
     * 文件名称(不带后缀)
     */
    @Size(max = 50, message = "文件名称(不带后缀)最大长度要小于 50")
    @NotBlank(message = "文件名称(不带后缀)不能为空")
    private String fileName;

    /**
     * 文件后缀
     */
    @Size(max = 50, message = "文件后缀最大长度要小于 50")
    @NotBlank(message = "文件后缀不能为空")
    private String extName;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件路径
     */
    @NotBlank(message = "文件路径不能为空")
    private String path;

    /**
     * 创建人员
     */
    @Size(max = 50, message = "创建人员最大长度要小于 50")
    private String createUser;

    /**
     * 创建日期
     */
    private Date createDate;

    /**
     * 修改人员
     */
    @Size(max = 50, message = "修改人员最大长度要小于 50")
    private String updateUser;

    /**
     * 修改日期
     */
    private Date updateDate;

    /**
     * md5码
     */
    @Size(max = 255, message = "md5码最大长度要小于 255")
    private String md5;

    private static final long serialVersionUID = 1L;
}