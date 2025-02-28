package com.xm.flowable.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProcessView {
    String base64;
    List<TaskInfoVo> taskInfoVoList;
}
