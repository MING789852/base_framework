package com.xm.flowable.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.Map;
@Data
public class TaskInfoVo{
    private String id;
    private String name;
    private String actName;
    private String description;
    private String title;
    private String businessNo;
    private String processInstanceId;
    private String executionId;
    private String owner;
    private String assignee;
    private String processDefinitionId;
    private Date createTime;
    private Map<String, Object> taskLocalVariables;
    private Map<String, Object> processVariables;
    private Date claimTime;
    private String actionTime;
    private String status;
    private String category;
    private String msg;
}
