package com.xm.util.dingding.requestParams;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDTaskUpdateParam {

    private String subject;
    private String description;
    private String dueTime;
    private boolean done;
    private List<String> executorIds;
    private List<String> participantIds;
}
