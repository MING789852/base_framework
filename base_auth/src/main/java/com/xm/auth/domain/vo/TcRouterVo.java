package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcRouter;
import lombok.Data;

import java.util.List;

@Data
public class TcRouterVo extends TcRouter {
    private Boolean isLeaf;

    private List<TcRouterVo> children;
}
