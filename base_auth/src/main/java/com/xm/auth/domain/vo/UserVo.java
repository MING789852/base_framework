package com.xm.auth.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class UserVo {
    private String username;

    private String nickName;

    private String userId;

    private List<String> roles;

    private String  accessToken;

    private String  refreshToken;

    private Date expires;
}
