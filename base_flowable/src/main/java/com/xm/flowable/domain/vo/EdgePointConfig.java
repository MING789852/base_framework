package com.xm.flowable.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class EdgePointConfig {
    private EdgePoint startPoint;
    private EdgePoint endPoint;
    //content2
    private List<EdgePoint> pointsList;
}
