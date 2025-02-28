package com.xm.fileAuth.domain.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ApplyFileDto {
    private String refId;
    private String refType;
    private String applyRemark;
    private Date effectiveTime;
    private List<String> fileIdList;
}
