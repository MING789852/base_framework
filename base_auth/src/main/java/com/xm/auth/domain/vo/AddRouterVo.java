package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcRouter;
import lombok.Data;

@Data
public class AddRouterVo {
    private TcRouter parentRouter;
    private TcRouter addRouter;
}
