package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcDept;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class TcDeptVo extends TcDept implements Serializable {
    private List<TcDeptVo> children;

    private Map<String,TcDeptVo> childrenMap;

    private Boolean isLeaf;
}
