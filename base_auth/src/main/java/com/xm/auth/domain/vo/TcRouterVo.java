package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcRouter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TcRouterVo extends TcRouter implements Serializable {
    private Boolean isLeaf;

    private List<TcRouterVo> children;
}
