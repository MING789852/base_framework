package com.xm.flowable.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NodeText {
    private BigDecimal x;
    private BigDecimal y;
    private String value;
}
