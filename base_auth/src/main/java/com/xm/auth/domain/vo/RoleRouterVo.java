package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcRouter;
import lombok.Data;

import java.util.List;

@Data
public class RoleRouterVo {
    private TcRole tcRole;
    private List<TcRouter> routerList;
}
