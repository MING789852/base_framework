package com.xm.auth.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoleRelUserVo  implements Serializable {
    private String userId;
    private String userName;
    private String nickName;
    private String jobNumber;
}
