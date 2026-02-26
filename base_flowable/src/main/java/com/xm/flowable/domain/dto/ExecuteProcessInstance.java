package com.xm.flowable.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExecuteProcessInstance {
    private String processInstanceId;
    private String taskId;
    private String userId;
    //指定目标
    //如果为空则为直接回滚、否则为指定回滚
    private List<String> rollbackTargetActIdList;
    //执行测试类型
    private String executeType;
    //原因
    private String msg;
    //转办人员
    private String transferUserId;
}
