package com.xm.module.dict.domain.query;

import lombok.Data;

@Data
public class DictQuery {
    private String dictMappingKey;
    private String dictGroupCode;
    private String dictQueryType;
}
