package com.xm.module.dict.domain.dto;

import com.xm.module.dict.domain.entity.TcDict;
import com.xm.module.dict.domain.entity.TcDictGroup;
import lombok.Data;

import java.util.List;

@Data
public class SaveOrUpdateDictDto {
    private TcDictGroup parentDictGroup;

    private TcDict parentDict;

    private List<TcDict> childrenList;
}
