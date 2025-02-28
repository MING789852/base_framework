package com.xm.fileAuth.enums;

import lombok.Getter;

@Getter
public enum FileApplyStatusEnum {
    approving(0),
    success(1),
    reject(2);
    private final Integer value;
    FileApplyStatusEnum(Integer value) {
        this.value = value;
    }
}
