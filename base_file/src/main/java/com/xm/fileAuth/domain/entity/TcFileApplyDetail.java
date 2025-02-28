package com.xm.fileAuth.domain.entity;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class TcFileApplyDetail implements Serializable {
    /**
    * 主键
    */
    @Size(max = 32,message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
    * 关联tc_file_apply表
    */
    @Size(max = 50,message = "关联tc_file_apply表最大长度要小于 50")
    private String applyId;

    /**
    * 文件ID
    */
    @Size(max = 32,message = "文件ID最大长度要小于 32")
    @NotBlank(message = "文件ID不能为空")
    private String fileId;

    /**
    * 文件名称
    */
    @Size(max = 50,message = "文件名称最大长度要小于 50")
    @NotBlank(message = "文件名称不能为空")
    private String fileName;

    private static final long serialVersionUID = 1L;
}