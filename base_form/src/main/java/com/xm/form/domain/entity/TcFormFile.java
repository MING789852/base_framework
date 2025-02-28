package com.xm.form.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 单据附件
 */
public class TcFormFile implements Serializable {
    /**
     * 主键
     */
    @Size(max = 32, message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
     * 关联id
     */
    @Size(max = 32, message = "关联id最大长度要小于 32")
    @NotBlank(message = "关联id不能为空")
    private String refId;

    /**
     * 关联类型
     */
    @Size(max = 50, message = "关联类型最大长度要小于 50")
    @NotBlank(message = "关联类型不能为空")
    private String refType;

    /**
     * 关联字段
     */
    @Size(max = 50, message = "关联字段最大长度要小于 50")
    @NotBlank(message = "关联字段不能为空")
    private String refField;

    /**
     * 序号
     */
    private Integer sequence;

    /**
     * 文件id
     */
    @Size(max = 50, message = "文件id最大长度要小于 50")
    @NotBlank(message = "文件id不能为空")
    private String fileId;

    /**
     * 文件名称
     */
    @Size(max = 100, message = "文件名称最大长度要小于 100")
    @NotBlank(message = "文件名称不能为空")
    private String fileName;

    /**
     * 文件类型
     */
    @Size(max = 50, message = "文件类型最大长度要小于 50")
    @NotBlank(message = "文件类型不能为空")
    private String fileType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 创建日期
     */
    private Date createDate;

    /**
     * 创建人员
     */
    @Size(max = 50, message = "创建人员最大长度要小于 50")
    private String createUser;

    /**
     * 创建人员id
     */
    @Size(max = 50, message = "创建人员id最大长度要小于 50")
    private String createUserId;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId == null ? null : refId.trim();
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType == null ? null : refType.trim();
    }

    public String getRefField() {
        return refField;
    }

    public void setRefField(String refField) {
        this.refField = refField == null ? null : refField.trim();
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId == null ? null : fileId.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType == null ? null : fileType.trim();
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId == null ? null : createUserId.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", refId=").append(refId);
        sb.append(", refType=").append(refType);
        sb.append(", refField=").append(refField);
        sb.append(", sequence=").append(sequence);
        sb.append(", fileId=").append(fileId);
        sb.append(", fileName=").append(fileName);
        sb.append(", fileType=").append(fileType);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", createDate=").append(createDate);
        sb.append(", createUser=").append(createUser);
        sb.append(", createUserId=").append(createUserId);
        sb.append("]");
        return sb.toString();
    }
}