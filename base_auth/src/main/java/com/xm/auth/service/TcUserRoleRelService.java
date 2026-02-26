package com.xm.auth.service;

import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcUserRoleRel;
import com.xm.auth.domain.vo.RoleRelUserVo;
import com.xm.auth.domain.vo.UserRoleRelVo;
import com.xm.module.core.params.QueryData;

import java.util.List;

public interface TcUserRoleRelService {
    List<TcUserRoleRel> selectByUserId(String userId);

    List<String>  getRoleIdListByUser(String userId);

    List<TcRole>  getRoleListByUser(String userId);

    //根据角色ID添加用户
    String roleAddUser(String roleId,List<String> userIdList);

    //根据角色编码添加用户
    String roleAddUserByRoleCode(String roleCode,List<String> userIdList);

    //用户添加角色(删除之后再新增)
    String saveUserRoleRel(UserRoleRelVo userRoleRelVo);

    String unRelUserAndRoleAll(String roleId, QueryData<RoleRelUserVo> queryData);

    String unRelUserAndRole(String roleId,String userId);

    void unRelByRoleIdList(List<String> roleIdList);

    int unRelByUserIdList(List<String> userIdList);
}
