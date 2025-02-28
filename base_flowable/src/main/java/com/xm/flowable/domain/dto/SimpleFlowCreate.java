package com.xm.flowable.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SimpleFlowCreate {
    @NotNull(message = "是否并行不能为空")
    //是否并行
    private boolean parallel;
    @NotEmpty(message = "审批人员不能为空")
    //审批人
    private List<SimpleFlowUser> flowUserList;

    public SimpleFlowCreate(boolean parallel, List<SimpleFlowUser> flowUserList) {
        this.parallel = parallel;
        this.flowUserList = flowUserList;
    }

    public SimpleFlowCreate() {
    }
}
