package com.xm.flowable.domain.dto;

import com.xm.flowable.domain.vo.ProcessBindData;
import lombok.Data;

import java.util.List;

@Data
public class SaveProcessBindData {
    private String groupName;
    private List<ProcessBindData> processBindDataList;
}
