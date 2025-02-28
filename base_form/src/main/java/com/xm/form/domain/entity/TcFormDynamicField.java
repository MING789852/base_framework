package com.xm.form.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 表单实例动态字段
 */
public class TcFormDynamicField implements Serializable {
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
     * 表单id
     */
    @Size(max = 32, message = "表单id最大长度要小于 32")
    @NotBlank(message = "表单id不能为空")
    private String formInsId;

    /**
     * 表单id
     */
    @Size(max = 32, message = "表单id最大长度要小于 32")
    @NotBlank(message = "表单id不能为空")
    private String formDetailId;

    /**
     * 文本字段
     */
    @Size(max = 255, message = "文本字段最大长度要小于 255")
    private String fieldText;

    /**
     * 大文本字段
     */
    private String fieldBigText;

    /**
     * 日期字段
     */
    private Date fieldDate;

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

    public String getFormInsId() {
        return formInsId;
    }

    public void setFormInsId(String formInsId) {
        this.formInsId = formInsId == null ? null : formInsId.trim();
    }

    public String getFormDetailId() {
        return formDetailId;
    }

    public void setFormDetailId(String formDetailId) {
        this.formDetailId = formDetailId == null ? null : formDetailId.trim();
    }

    public String getFieldText() {
        return fieldText;
    }

    public void setFieldText(String fieldText) {
        this.fieldText = fieldText == null ? null : fieldText.trim();
    }

    public String getFieldBigText() {
        return fieldBigText;
    }

    public void setFieldBigText(String fieldBigText) {
        this.fieldBigText = fieldBigText == null ? null : fieldBigText.trim();
    }

    public Date getFieldDate() {
        return fieldDate;
    }

    public void setFieldDate(Date fieldDate) {
        this.fieldDate = fieldDate;
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
        sb.append(", formInsId=").append(formInsId);
        sb.append(", formDetailId=").append(formDetailId);
        sb.append(", fieldText=").append(fieldText);
        sb.append(", fieldBigText=").append(fieldBigText);
        sb.append(", fieldDate=").append(fieldDate);
        sb.append("]");
        return sb.toString();
    }
}