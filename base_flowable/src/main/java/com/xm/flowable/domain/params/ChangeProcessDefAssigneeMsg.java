package com.xm.flowable.domain.params;

import com.xm.flowable.domain.res.FlowableMsgCreate;
import lombok.Data;

import java.util.List;

@Data
public class ChangeProcessDefAssigneeMsg {
    private String businessType;
    private String businessKey;
    //旧接收人（删除）
    private List<String> oldAssigneeList;
    //新接收人消息 （新消息）
    private List<FlowableMsgCreate> newFlowableMsgCreates;
}
