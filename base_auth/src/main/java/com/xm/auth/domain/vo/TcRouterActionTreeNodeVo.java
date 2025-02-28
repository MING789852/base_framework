package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcRouterAction;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TcRouterActionTreeNodeVo extends TcRouterAction {
    private Boolean isLeaf;
    private String title;
}
