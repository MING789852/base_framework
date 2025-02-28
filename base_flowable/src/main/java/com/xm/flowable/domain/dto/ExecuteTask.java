package com.xm.flowable.domain.dto;

import com.xm.auth.domain.entity.TcUser;
import com.xm.flowable.enums.TaskStatusEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ExecuteTask {
    private TaskStatusEnum status;
    private String taskId;
    private String msg;
    private TcUser user;
    private Map<String,Object> variable;
}
