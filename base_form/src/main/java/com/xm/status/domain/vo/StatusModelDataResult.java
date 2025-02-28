package com.xm.status.domain.vo;

import com.xm.status.domain.dto.TcStatusDetailInstanceDto;
import lombok.Data;

import java.util.List;

@Data
public class StatusModelDataResult {
    private List<TcStatusDetailInstanceDto> detailInstanceDtoList;
}
