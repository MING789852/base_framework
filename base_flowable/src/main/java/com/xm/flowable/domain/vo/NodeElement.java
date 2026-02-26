package com.xm.flowable.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NodeElement {
    private String id;
    private BigDecimal x;
    private BigDecimal y;
    //节点类型
    private String type;
    //节点文本
    private NodeText text;
    private ElementProp properties;
}
