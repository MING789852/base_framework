package com.xm.auth.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAndDeptVo  implements Serializable {
    private String userId;
    private String deptId;
    private String deptName;
    private String userName;
}
