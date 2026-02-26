package com.xm.flowable.domain.dto;

import com.xm.flowable.domain.vo.EdgeElement;
import com.xm.flowable.domain.vo.NodeElement;
import lombok.Data;

@Data
public class ChangeModel {
    NodeElement node;
    EdgeElement edge;
    String processDefinitionKey;
}
