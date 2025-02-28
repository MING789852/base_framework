package com.xm.core.msg.consts;

import lombok.Data;

@Data
public class MsgTypeItem {
    private String code;
    private String label;

    public MsgTypeItem(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
