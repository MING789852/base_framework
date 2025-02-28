package com.xm.auth.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.vo.RoleRelUserVo;
import com.xm.auth.mapper.TcRoleMapper;
import com.xm.module.core.params.QueryData;

import java.util.List;

public interface TcRoleService {
    List<TcRole>  getRoleList();

    Page<TcRole> selectByPage(QueryData<TcRole> queryData);

    String deleteData(List<TcRole> tcRoleList);

    String saveOrUpdateData(List<TcRole> tcRoleList);

    Page<RoleRelUserVo> selectUserAndDeptPageByRoleId(String roleId, QueryData<RoleRelUserVo> queryData);

    TcRoleMapper getMapper();
}
