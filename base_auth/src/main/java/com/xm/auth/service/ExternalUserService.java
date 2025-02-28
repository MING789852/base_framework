package com.xm.auth.service;

import com.xm.auth.domain.result.UserTypeResult;

import java.util.List;

public interface ExternalUserService {

    /**
     * 根据部门id创建用户
     */
    List<UserTypeResult> getUserTypeMapping();
}
