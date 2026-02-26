package com.xm.auth.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class UserVo implements Serializable {
    private String username;

    private String nickName;

    private String userId;

    private String  accessToken;

    private String  refreshToken;

    private Date expires;

    private Integer userType;

    private String email;
}
