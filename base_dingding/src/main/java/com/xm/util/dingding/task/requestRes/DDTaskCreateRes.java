package com.xm.util.dingding.task.requestRes;

import com.xm.util.dingding.task.commonParams.DDTaskContentFieldListDTO;
import com.xm.util.dingding.task.commonParams.NotifyConfigsDTO;
import com.xm.util.dingding.task.commonParams.DetailUrlDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDTaskCreateRes {
    private boolean result;

    //正常返回
    private String id;
    private String subject;
    private String description;
    private long startTime;
    private long dueTime;
    private long finishTime;
    private boolean done;
    private List<String> executorIds;
    private List<String> participantIds;
    private String source;
    private String sourceId;
    private long createdTime;
    private long modifiedTime;
    private String creatorId;
    private String modifierId;
    private String bizTag;
    private String requestId;
    private boolean isOnlyShowExecutor;
    private int priority;
    private DetailUrlDTO detailUrl;
    private NotifyConfigsDTO notifyConfigs;
    private NotifyConfigsDTO remindNotifyConfigs;
    private Long reminderTimeStamp;
    private List<DDTaskContentFieldListDTO> contentFieldList;

    //错误返回
    private String code;
    private String message;
}
