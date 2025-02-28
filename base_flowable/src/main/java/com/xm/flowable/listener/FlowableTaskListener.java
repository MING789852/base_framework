package com.xm.flowable.listener;

import com.xm.flowable.domain.vo.ProcessInstanceVo;
import com.xm.flowable.domain.vo.TaskInfoVo;
import com.xm.flowable.enums.TaskStatusEnum;
import liquibase.pro.packaged.S;

import java.io.Serializable;
import java.util.Map;


public interface FlowableTaskListener extends Serializable {

    void execute(ProcessInstanceVo processInstance, TaskInfoVo taskInfoVo, TaskStatusEnum taskStatusEnum, boolean isFinish, Map<String,Object> otherVariable);
}
