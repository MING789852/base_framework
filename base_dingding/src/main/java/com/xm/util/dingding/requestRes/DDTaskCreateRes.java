package com.xm.util.dingding.requestRes;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDTaskCreateRes {
    private String id;
    private String subject;
    private String description;
    private long startTime;
    private long dueTime;
    private long finishTime;
    private boolean done;
    private List<String> executorIds;
    private List<String> participantIds;
    private DetailUrlDTO detailUrl;
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
    private NotifyConfigsDTO notifyConfigs;

    @NoArgsConstructor
    @Data
    public static class DetailUrlDTO {
        private String pcUrl;
        private String appUrl;
    }

    @NoArgsConstructor
    @Data
    public static class NotifyConfigsDTO {
        private String dingNotify;
    }
}
