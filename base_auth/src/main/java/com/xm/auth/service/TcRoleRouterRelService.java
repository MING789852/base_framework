package com.xm.auth.service;

import com.xm.auth.domain.entity.TcRoleRouterRel;
import com.xm.auth.domain.vo.RoleRouterVo;

import java.util.List;

public interface TcRoleRouterRelService {
    //根据角色ID获取角色路由关联
    List<TcRoleRouterRel> selectByRoleIdList(List<String> roleIdList);

    //根据路由ID删除角色路由关联表
    void deleteByRouterIdList(List<String> routerIdList);

    //根据角色ID删除角色路由关联表
    void deleteByRoleIdList(List<String> roleIdList);

    //保存角色路由关联
    String saveRoleRouterRel(RoleRouterVo roleRouterVo);

    //根据角色ID获取路由ID
    List<String>  getRouterIdListByRole(String roleId);
}
