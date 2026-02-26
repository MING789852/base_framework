package com.xm.flowable.domain.res;

import com.xm.core.msg.params.JumpUrlParam;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FlowableMsgCreate {
    private String title;
    private String content;
    private List<String> msgTypeList;
    private List<String> userIdList;
    private JumpUrlParam jumpUrlParam;
    private Map<String,Object> otherMap;
}
