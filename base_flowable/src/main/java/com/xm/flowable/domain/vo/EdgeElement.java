package com.xm.flowable.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class EdgeElement {
    private String id;
    private String type;
    private NodeText text;
    private String sourceNodeId;
    private String targetNodeId;
    private ElementProp properties;
    private EdgePoint startPoint;
    private EdgePoint endPoint;
    private List<EdgePoint> pointsList;
}
