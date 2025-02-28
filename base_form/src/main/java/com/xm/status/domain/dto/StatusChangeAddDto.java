package com.xm.status.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class StatusChangeAddDto {
    private String statusMainModelId;
    private String statusDetailModelId;
    private List<TcStatusDetailInstanceDto> statusList;
}
