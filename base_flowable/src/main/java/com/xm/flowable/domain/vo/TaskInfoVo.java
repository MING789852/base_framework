package com.xm.flowable.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.Map;
@Data
public class TaskInfoVo{
    private String id;
    //审批人
    private String name;
    private String actName;
    private String description;
    private String title;
    private String businessNo;
    private String processInstanceId;
    private String executionId;
    private String owner;
    //审批人id
    private String assignee;
    private String processDefinitionId;
    //开始时间
    private Date createTime;
    private Map<String, Object> taskLocalVariables;
    private Map<String, Object> processVariables;
    private Date claimTime;
    //审批时间
    private String actionTime;
    private String status;
    private String category;
    //审批意见
    private String msg;

    private boolean his;
}
