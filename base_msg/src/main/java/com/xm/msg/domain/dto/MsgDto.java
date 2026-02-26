package com.xm.msg.domain.dto;


import lombok.Data;

import java.util.List;

@Data
public class MsgDto{
    private String title;
    private String content;
    private String businessType;
    private String businessKey;
    private String type;
    private List<String> toUserIdList;
    private String jumpUrl;
    private String groupId;
    private boolean groupAtAll;
}
