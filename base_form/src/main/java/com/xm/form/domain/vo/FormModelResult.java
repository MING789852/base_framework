package com.xm.form.domain.vo;

import com.xm.module.dict.domain.query.DictQuery;
import com.xm.form.domain.params.DyFormColumn;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FormModelResult {
    private List<DyFormColumn> formDefines;
    private List<DictQuery>  dictQueryType;
    private Map<String,Object> dataMap;
}
