package com.xm.flowable.domain.dto;

import lombok.Data;
import org.flowable.task.api.Task;

@Data
public class RollBackWithData {
    //回滚是否记录信息
    private final boolean withRecord;
    private final Task task;
    private final String rollBackRecordMsg;

    public RollBackWithData(boolean withRecord, Task task, String recordMsg) {
        this.withRecord = withRecord;
        this.task = task;
        this.rollBackRecordMsg = recordMsg;
    }
}
