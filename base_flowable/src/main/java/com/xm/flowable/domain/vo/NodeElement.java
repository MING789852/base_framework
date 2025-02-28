package com.xm.flowable.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class NodeElement {
    private String id;
    private BigDecimal x;
    private BigDecimal y;
    private String type;
    private NodeText text;
    private NodeProp properties;
}
