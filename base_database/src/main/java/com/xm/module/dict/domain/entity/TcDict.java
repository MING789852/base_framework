package com.xm.module.dict.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 字典组子项
 */
public class TcDict implements Serializable {
    /**
     * 主键
     */
    @Size(max = 32, message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
     * 分组
     */
    @Size(max = 32, message = "分组最大长度要小于 32")
    @NotBlank(message = "分组不能为空")
    private String groupKey;

    /**
     * 编码
     */
    @Size(max = 100, message = "编码最大长度要小于 100")
    @NotBlank(message = "编码不能为空")
    private String dictCode;

    /**
     * 编码描述
     */
    @Size(max = 255, message = "编码描述最大长度要小于 255")
    @NotBlank(message = "编码描述不能为空")
    private String dictLabel;

    /**
     * 是否可用
     */
    @NotNull(message = "是否可用不能为null")
    private Integer judgeEnable;

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
     * 层级
     */
    @NotNull(message = "层级不能为null")
    private Integer level;

    /**
     * 父id
     */
    @Size(max = 50, message = "父id最大长度要小于 50")
    private String parentId;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey == null ? null : groupKey.trim();
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode == null ? null : dictCode.trim();
    }

    public String getDictLabel() {
        return dictLabel;
    }

    public void setDictLabel(String dictLabel) {
        this.dictLabel = dictLabel == null ? null : dictLabel.trim();
    }

    public Integer getJudgeEnable() {
        return judgeEnable;
    }

    public void setJudgeEnable(Integer judgeEnable) {
        this.judgeEnable = judgeEnable;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId == null ? null : parentId.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", groupKey=").append(groupKey);
        sb.append(", dictCode=").append(dictCode);
        sb.append(", dictLabel=").append(dictLabel);
        sb.append(", judgeEnable=").append(judgeEnable);
        sb.append(", createUser=").append(createUser);
        sb.append(", createDate=").append(createDate);
        sb.append(", updateUser=").append(updateUser);
        sb.append(", updateDate=").append(updateDate);
        sb.append(", level=").append(level);
        sb.append(", parentId=").append(parentId);
        sb.append("]");
        return sb.toString();
    }
}