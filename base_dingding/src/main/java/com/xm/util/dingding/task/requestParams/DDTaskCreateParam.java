package com.xm.util.dingding.task.requestParams;

import com.xm.util.dingding.task.commonParams.DDTaskContentFieldListDTO;
import com.xm.util.dingding.task.commonParams.DetailUrlDTO;
import com.xm.util.dingding.task.commonParams.NotifyConfigsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDTaskCreateParam {

    private String sourceId;
    private String subject;
    private String creatorId;
    private String description;
    private long dueTime;
    private List<String> executorIds;
    private List<String> participantIds;
    private boolean isOnlyShowExecutor;
    private int priority;
    private DetailUrlDTO detailUrl;
    private String todoType;
    private NotifyConfigsDTO notifyConfigs;
    private NotifyConfigsDTO remindNotifyConfigs;
    private Long reminderTimeStamp;
    private List<DDTaskContentFieldListDTO> contentFieldList;
    private String cardTypeId;
}
