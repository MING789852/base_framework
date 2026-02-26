package com.xm.flowable.domain.vo;

import lombok.Data;

import java.util.Date;

@Data
public class FlowableShowDataVo {
    private String jumpUrl;
    private String flowableName;
    private String businessType;
    private String createUser;
    private Date startDate;
    private Date endDate;
    private String finish;
    private ProcessInstanceVo instance;
}
