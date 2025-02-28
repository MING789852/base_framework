package com.xm.status.domain.vo;

import com.xm.status.domain.entity.TcStatusMainInstance;
import lombok.Data;

@Data
public class StatusModelSaveOrUpdateResult {
    private TcStatusMainInstance instance;
    private String activeStatus;
}
