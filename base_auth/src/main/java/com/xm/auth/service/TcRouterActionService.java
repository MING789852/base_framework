package com.xm.auth.service;

import com.xm.auth.domain.dto.RouterActionSaveDataDto;
import com.xm.auth.domain.dto.RouterActionSaveRefDto;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcRoleRouterActionRel;
import com.xm.auth.domain.entity.TcRouter;
import com.xm.auth.domain.entity.TcRouterAction;
import com.xm.auth.domain.query.RouterActionAuthQuery;
import com.xm.auth.domain.vo.RouterWithActionTreeNodeVo;

import java.util.List;
import java.util.Map;

public interface TcRouterActionService{

    //根据路由获取路由操作
    List<TcRouterAction> getRouterActionDataByRouter(TcRouter router);

    //保存路由和路由操作的关系
    String saveRouterActionData(RouterActionSaveDataDto saveDto);

    //删除路由操作，同时删除对应关联表
    String deleteRouterActionData(List<TcRouterAction> routerActionList);

    //保存角色和路由操作关系
    String saveRouterActionRef(RouterActionSaveRefDto saveRefDto);

    //获取角色对应RouterActionId
    //用于展示角色对应有哪些路由操作
    List<String> getRouterActionRefByRole(TcRole role);

    List<TcRouterAction> getRouterActionByRoleAndRouter(List<String> roleIdList,List<String> routerIdList);

    List<TcRouterAction> getRouterActionDataByIdList(List<String> idList);

    List<TcRoleRouterActionRel> getRefByRoleAndRouter(List<String> roleIdList,List<String> routerIdList);

    //根据路由ID删除对应的路由操作关联
    //用于路由删除时使用
    void deleteRouterActionDataAndRelByRouterIdList(List<String> routerIdList);

    //根据角色ID删除对应的路由操作关联
    //用于角色删除时使用
    void deleteRouterActionRelByRoleIdList(List<String> roleIdList);

    //根据角色ID和路由ID删除对应的路由操作关联
    //用于角色解除路由关联时使用
    void deleteRouterActionRelByRoleIdAndRouterIdList(String roleId,List<String> routerIdList);
}
