package com.xm.auth.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserRoleRelVo implements Serializable {
    private String userId;
    private List<String> roleIdList;
}
