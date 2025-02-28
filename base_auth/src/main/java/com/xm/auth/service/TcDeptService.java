package com.xm.auth.service;

import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.TcDeptVo;
import com.xm.auth.domain.vo.UserAndDeptVo;
import com.xm.module.core.params.QueryData;

import java.util.List;

public interface TcDeptService {
    /**
     * 初始化部门
     */
    String initDept();

    List<TcDeptVo> selectByList(QueryData queryData);

    List<TcDeptVo> selectAllByList(QueryData queryData);

    TcDeptVo selectOne(String id);

    List<TcDeptVo> findChildrenList(TcDeptVo parent);

    List<UserAndDeptVo> findUserAndDeptRefByDeptId(String deptId);

    /**
     * 根据部门id创建用户
     */
    List<TcUser> createTcUserWithDeptId(String deptId);



    String allocateRoleToDept(String deptId,List<String> roleIdList);
}
