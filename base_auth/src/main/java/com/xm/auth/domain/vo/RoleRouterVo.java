package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcRouter;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoleRouterVo implements Serializable {
    private TcRole tcRole;
    private List<TcRouter> routerList;
}
