package com.xm.auth.service;

import com.xm.auth.domain.entity.TcUser;

import java.util.List;

public interface ExternalDeptService {

    /**
     * 根据部门id创建用户
     */
    List<TcUser> createTcUserWithDeptId(String deptId);


    /**
     * 初始化部门
     */
    String initDept();
}
