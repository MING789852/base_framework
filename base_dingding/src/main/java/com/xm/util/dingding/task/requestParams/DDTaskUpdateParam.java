package com.xm.util.dingding.task.requestParams;

import com.xm.util.dingding.task.commonParams.DDTaskContentFieldListDTO;
import com.xm.util.dingding.task.commonParams.DetailUrlDTO;
import com.xm.util.dingding.task.commonParams.NotifyConfigsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDTaskUpdateParam {

    private String subject;
    private String description;
    private Long dueTime;
    private boolean done;
    private List<String> executorIds;
    private List<String> participantIds;
    private DetailUrlDTO detailUrl;
    private NotifyConfigsDTO notifyConfigs;
    private NotifyConfigsDTO remindNotifyConfigs;
    private Long reminderTimeStamp;
    private List<DDTaskContentFieldListDTO> contentFieldList;
}
