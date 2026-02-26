package com.xm.util.dingding.task.requestRes;


import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DDPersonalTaskCreateRes {
    private String taskId;
    private boolean done;
    private Long createdTime;
}
