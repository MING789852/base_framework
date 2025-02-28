package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoginUserVo extends TcUser {
    private String loginTime;
    private String sessionId;
}
