package com.xm.auth.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserRoleRelVo {
    private String userId;
    private List<String> roleIdList;
}
