package com.xm.module.dict.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class DictTree {
    private String value;
    private String label;
    private Boolean isLeaf;
    private List<DictTree> children;
}
