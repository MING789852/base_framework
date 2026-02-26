package com.xm.auth.service;

import cn.hutool.json.JSONObject;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcRouter;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.AddRouterVo;
import com.xm.auth.domain.vo.RouterWithActionTreeNodeVo;
import com.xm.auth.domain.vo.TcRouterVo;

import java.util.List;

public interface TcRouterService {


    JSONObject getCurrentRouter();


    List<TcRouterVo> getRouterList();

    List<TcRouterVo> getPrivateRouterList();


    String addRouter(AddRouterVo addRouterVo);


    String updateRouter(TcRouter router);


    String deleteRouter(List<TcRouter> tcRouterList);


    List<TcRouter> getPrivateRouter(List<TcRole> roleList);

    List<TcRouter> getPublicRouter();

    TcRouter getTcRouterByRouterName(String routerName);

    List<TcRouter> getPrivateRouterByRole(TcRole role);

    List<RouterWithActionTreeNodeVo> getRouterWithAction(TcRole role);
}
