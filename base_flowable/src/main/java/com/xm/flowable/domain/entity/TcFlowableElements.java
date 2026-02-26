package com.xm.flowable.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 流程图节点
 */
@Data
@TableName(value = "tc_flowable_elements")
public class TcFlowableElements implements Serializable {
    private static final long serialVersionUID = 1L;

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
    @TableField(value = "process_definition_name")
    @Size(max = 100, message = "流程定义名称最大长度要小于 100")
    @NotBlank(message = "流程定义名称不能为空")
    private String processDefinitionName;

    /**
     * 节点类型
     */
    @TableField(value = "element_type")
    @Size(max = 100, message = "节点类型最大长度要小于 100")
    @NotBlank(message = "节点类型不能为空")
    private String elementType;

    /**
     * 节点x坐标
     */
    @TableField(value = "element_x")
    private BigDecimal elementX;

    /**
     * 节点y坐标
     */
    @TableField(value = "element_y")
    private BigDecimal elementY;

    /**
     * 节点文本x坐标
     */
    @TableField(value = "text_x")
    private BigDecimal textX;

    /**
     * 节点文本y坐标
     */
    @TableField(value = "text_y")
    private BigDecimal textY;

    /**
     * 节点人员key
     */
    @TableField(value = "assignee")
    @Size(max = 100, message = "节点人员key最大长度要小于 100")
    private String assignee;

    /**
     * 节点人员名称
     */
    @TableField(value = "assignee_name")
    @Size(max = 100, message = "节点人员名称最大长度要小于 100")
    private String assigneeName;

    /**
     * 连线源节点id
     */
    @TableField(value = "source_node_Id")
    @Size(max = 100, message = "连线源节点id最大长度要小于 100")
    private String sourceNodeId;

    /**
     * 连线目标节点id
     */
    @TableField(value = "target_node_Id")
    @Size(max = 100, message = "连线目标节点id最大长度要小于 100")
    private String targetNodeId;

    /**
     * 预留字段1
     */
    @TableField(value = "content1")
    private String content1;

    /**
     * 预留字段2
     */
    @TableField(value = "content2")
    private String content2;

    /**
     * 预留字段3
     */
    @TableField(value = "content3")
    @Size(max = 100, message = "预留字段3最大长度要小于 100")
    private String content3;

    /**
     * 预留字段4
     */
    @TableField(value = "content4")
    @Size(max = 100, message = "预留字段4最大长度要小于 100")
    private String content4;

    /**
     * 节点名称
     */
    @TableField(value = "element_name")
    @Size(max = 100, message = "节点名称最大长度要小于 100")
    private String elementName;

    /**
     * 预留字段5
     */
    @TableField(value = "content5")
    private String content5;

    /**
     * 预留字段6
     */
    @TableField(value = "content6")
    private String content6;
}