package com.xm.util.dingding.requestParams;

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
    private DetailUrlDTO detailUrl;
    private boolean isOnlyShowExecutor;
    private int priority;
    private NotifyConfigsDTO notifyConfigs;

    @NoArgsConstructor
    @Data
    public static class DetailUrlDTO {
        private String appUrl;
        private String pcUrl;
    }

    @NoArgsConstructor
    @Data
    public static class NotifyConfigsDTO {
        private String dingNotify;
    }
}
