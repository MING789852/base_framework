package com.xm.auth.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.vo.RoleRelUserVo;
import com.xm.auth.mapper.TcRoleMapper;
import com.xm.core.params.ColumnProps;
import com.xm.module.core.params.QueryData;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface TcRoleService {
    List<TcRole>  getRoleList();

    List<TcRole>  selectRoleByRoleCodeList(List<String> roleCodeList);

    Page<TcRole> selectByPage(QueryData<TcRole> queryData);

    String deleteData(List<TcRole> tcRoleList);

    String saveOrUpdateData(List<TcRole> tcRoleList);

    Page<RoleRelUserVo> selectUserPageByRoleId(String roleId, QueryData<RoleRelUserVo> queryData);

    void exportRoleUsersExcel(String roleId, QueryData<RoleRelUserVo> queryData, HttpServletResponse  response);

    List<ColumnProps> getRoleUsersColumnProps();

    TcRoleMapper getMapper();
}
