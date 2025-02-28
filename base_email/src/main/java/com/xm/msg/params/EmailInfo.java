package com.xm.msg.params;

import lombok.Data;

import java.util.List;

@Data
public class EmailInfo {
    private String title;
    private String content;
    private List<String> toUserList;
    private List<String> ccUserList;
    private String fromEmail;
    private String sender;
}
