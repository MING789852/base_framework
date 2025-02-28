package com.xm.flowable.domain.query;

import com.xm.flowable.domain.entity.TcFlowableModel;
import lombok.Data;

@Data
public class ProcessInstancePageQuery {
    private TcFlowableModel model;
    private boolean his;
    private long size=10;
    private long current=1;

    private String name;
    private String businessKey;
}
