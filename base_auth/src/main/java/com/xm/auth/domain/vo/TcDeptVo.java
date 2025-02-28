package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcDept;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TcDeptVo extends TcDept {
    private List<TcDeptVo> children;

    private Map<String,TcDeptVo> childrenMap;

    private Boolean isLeaf;
}
