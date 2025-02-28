package com.xm.fileAuth.domain.dto;

import com.xm.fileAuth.domain.entity.TcFileApply;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AuthApplyFileDto {
    private List<TcFileApply> list;
    //关联FileApplyStatusEnum
    private Integer applyStatus;
    private Date effectiveTime;
}
