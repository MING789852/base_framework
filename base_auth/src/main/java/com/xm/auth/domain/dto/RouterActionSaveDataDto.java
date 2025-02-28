package com.xm.auth.domain.dto;

import com.xm.auth.domain.entity.TcRouter;
import com.xm.auth.domain.entity.TcRouterAction;
import lombok.Data;

import java.util.List;

@Data
public class RouterActionSaveDataDto {
    private TcRouter router;
    private List<TcRouterAction> routerActionList;
}
