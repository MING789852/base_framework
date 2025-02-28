package com.xm.auth.service;


import com.xm.auth.mapper.TcUserDeptRelMapper;

import java.util.List;

public interface UserDeptRelService {
    /**
     * 关联部门
     */
    void relatedDepartments(String userId, List<Integer> deptIdList);

    void relatedByUserIdAndDeptId(String userId,String deptId);

    void delRelatedByUserId(String userId);

    void delRelatedByDeptId(String deptId);

    TcUserDeptRelMapper getMapper();
}
