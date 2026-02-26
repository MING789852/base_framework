package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoginUserVo extends TcUser implements Serializable {
    private String loginTime;
    private String sessionId;
}
