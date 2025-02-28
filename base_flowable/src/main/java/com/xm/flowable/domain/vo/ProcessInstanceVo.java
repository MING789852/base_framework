package com.xm.flowable.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class ProcessInstanceVo {
    private String id;
    private Map<String, Object> processVariables;
    private String businessKey;
    private String processDefinitionId;
    private Date startTime;
    private boolean isSuspended;
    private boolean isEnded;
    private boolean his;
    private String processDefinitionKey;
    private String processDefinitionName;
    private Integer processDefinitionVersion;
    private String name;
    private String description;
    private String businessStatus;
    private String deploymentId;

    private Date endTime;
    private boolean isDeleted;
    private String deleteReason;
}
