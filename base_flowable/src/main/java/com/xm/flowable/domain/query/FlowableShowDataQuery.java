package com.xm.flowable.domain.query;

import lombok.Data;

@Data
public class FlowableShowDataQuery {
    private String businessType;
    //流程是否结束
    private Integer finish;


    private int size=5;
    private int current=1;
}
