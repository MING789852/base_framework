package com.xm.flowable.domain.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 流程图节点
 */
public class TcFlowableElements implements Serializable {
    /**
     * 流程定义key
     */
    @Size(max = 100, message = "流程定义key最大长度要小于 100")
    @NotBlank(message = "流程定义key不能为空")
    private String processDefinitionKey;

    /**
     * 节点id
     */
    @Size(max = 100, message = "节点id最大长度要小于 100")
    @NotBlank(message = "节点id不能为空")
    private String elementId;

    /**
     * 流程定义名称
     */
    @Size(max = 100, message = "流程定义名称最大长度要小于 100")
    @NotBlank(message = "流程定义名称不能为空")
    private String processDefinitionName;

    /**
     * 节点类型
     */
    @Size(max = 100, message = "节点类型最大长度要小于 100")
    @NotBlank(message = "节点类型不能为空")
    private String elementType;

    /**
     * 节点x坐标
     */
    private BigDecimal elementX;

    /**
     * 节点y坐标
     */
    private BigDecimal elementY;

    /**
     * 节点文本x坐标
     */
    private BigDecimal textX;

    /**
     * 节点文本y坐标
     */
    private BigDecimal textY;

    /**
     * 节点人员key
     */
    @Size(max = 100, message = "节点人员key最大长度要小于 100")
    private String assignee;

    /**
     * 节点人员名称
     */
    @Size(max = 100, message = "节点人员名称最大长度要小于 100")
    private String assigneeName;

    /**
     * 连线源节点id
     */
    @Size(max = 100, message = "连线源节点id最大长度要小于 100")
    private String sourceNodeId;

    /**
     * 连线目标节点id
     */
    @Size(max = 100, message = "连线目标节点id最大长度要小于 100")
    private String targetNodeId;

    /**
     * 预留字段1
     */
    @Size(max = 100, message = "预留字段1最大长度要小于 100")
    private String content1;

    /**
     * 预留字段2
     */
    @Size(max = 100, message = "预留字段2最大长度要小于 100")
    private String content2;

    /**
     * 预留字段3
     */
    @Size(max = 100, message = "预留字段3最大长度要小于 100")
    private String content3;

    /**
     * 预留字段4
     */
    @Size(max = 100, message = "预留字段4最大长度要小于 100")
    private String content4;

    /**
     * 节点名称
     */
    @Size(max = 100, message = "节点名称最大长度要小于 100")
    private String elementName;

    private static final long serialVersionUID = 1L;

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey == null ? null : processDefinitionKey.trim();
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId == null ? null : elementId.trim();
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName == null ? null : processDefinitionName.trim();
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType == null ? null : elementType.trim();
    }

    public BigDecimal getElementX() {
        return elementX;
    }

    public void setElementX(BigDecimal elementX) {
        this.elementX = elementX;
    }

    public BigDecimal getElementY() {
        return elementY;
    }

    public void setElementY(BigDecimal elementY) {
        this.elementY = elementY;
    }

    public BigDecimal getTextX() {
        return textX;
    }

    public void setTextX(BigDecimal textX) {
        this.textX = textX;
    }

    public BigDecimal getTextY() {
        return textY;
    }

    public void setTextY(BigDecimal textY) {
        this.textY = textY;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee == null ? null : assignee.trim();
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName == null ? null : assigneeName.trim();
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(String sourceNodeId) {
        this.sourceNodeId = sourceNodeId == null ? null : sourceNodeId.trim();
    }

    public String getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(String targetNodeId) {
        this.targetNodeId = targetNodeId == null ? null : targetNodeId.trim();
    }

    public String getContent1() {
        return content1;
    }

    public void setContent1(String content1) {
        this.content1 = content1 == null ? null : content1.trim();
    }

    public String getContent2() {
        return content2;
    }

    public void setContent2(String content2) {
        this.content2 = content2 == null ? null : content2.trim();
    }

    public String getContent3() {
        return content3;
    }

    public void setContent3(String content3) {
        this.content3 = content3 == null ? null : content3.trim();
    }

    public String getContent4() {
        return content4;
    }

    public void setContent4(String content4) {
        this.content4 = content4 == null ? null : content4.trim();
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName == null ? null : elementName.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", processDefinitionKey=").append(processDefinitionKey);
        sb.append(", elementId=").append(elementId);
        sb.append(", processDefinitionName=").append(processDefinitionName);
        sb.append(", elementType=").append(elementType);
        sb.append(", elementX=").append(elementX);
        sb.append(", elementY=").append(elementY);
        sb.append(", textX=").append(textX);
        sb.append(", textY=").append(textY);
        sb.append(", assignee=").append(assignee);
        sb.append(", assigneeName=").append(assigneeName);
        sb.append(", sourceNodeId=").append(sourceNodeId);
        sb.append(", targetNodeId=").append(targetNodeId);
        sb.append(", content1=").append(content1);
        sb.append(", content2=").append(content2);
        sb.append(", content3=").append(content3);
        sb.append(", content4=").append(content4);
        sb.append(", elementName=").append(elementName);
        sb.append("]");
        return sb.toString();
    }
}