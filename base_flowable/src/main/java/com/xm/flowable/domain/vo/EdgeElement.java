package com.xm.flowable.domain.vo;

import lombok.Data;

import java.util.Map;

@Data
public class EdgeElement {
    private String id;
    private String type;
    private NodeText text;
    private String sourceNodeId;
    private String targetNodeId;
    private NodeProp properties;
}
