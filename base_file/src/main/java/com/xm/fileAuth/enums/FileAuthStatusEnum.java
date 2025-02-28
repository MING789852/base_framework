package com.xm.fileAuth.enums;

import lombok.Getter;

@Getter
public enum FileAuthStatusEnum {
    ALL_UN_APPLY(6000,"全部文件未申请"),
    ALL_APPLYING(6001,"全部文件审批中"),
    PART_APPLYING(6002,"部分文件申请审批中"),
    APPLY_ALLOW(0,"申请成功");

    private final Integer value;
    private final String label;

    FileAuthStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
