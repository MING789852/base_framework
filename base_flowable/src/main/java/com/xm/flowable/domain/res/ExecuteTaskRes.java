package com.xm.flowable.domain.res;

import com.xm.flowable.domain.vo.TaskInfoVo;
import com.xm.flowable.enums.TaskStatusEnum;
import lombok.Data;
import org.flowable.engine.runtime.ProcessInstance;

@Data
public class ExecuteTaskRes {
    private ProcessInstance processInstance;
    private TaskInfoVo taskInfoVo;
    private TaskStatusEnum taskStatusEnum;
    private boolean isFinish;
}
