package com.xm.flowable.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class TaskVariableVo {
    private String taskId;
    private String name;
    private List<TaskVariableDetailVo> variableList;
}
