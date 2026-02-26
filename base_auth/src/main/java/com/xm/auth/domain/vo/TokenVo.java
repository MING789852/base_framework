package com.xm.auth.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenVo implements Serializable {
    String accessToken;
    String refreshToken;
}
