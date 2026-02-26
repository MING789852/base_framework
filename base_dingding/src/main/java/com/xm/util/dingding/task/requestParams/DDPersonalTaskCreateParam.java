package com.xm.util.dingding.task.requestParams;

import com.xm.util.dingding.task.commonParams.NotifyConfigsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDPersonalTaskCreateParam{
    private String subject;
    private String description;
    private Long dueTime;
    private List<String> executorIds;
    private List<String> participantIds;
    private NotifyConfigsDTO notifyConfigs;
}
