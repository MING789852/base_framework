package com.xm.flowable.domain.vo;

import com.xm.flowable.domain.entity.TcFlowableModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlowableModelVo extends TcFlowableModel {
    private List<EdgeElement> edges;
    private List<NodeElement> nodes;
}
