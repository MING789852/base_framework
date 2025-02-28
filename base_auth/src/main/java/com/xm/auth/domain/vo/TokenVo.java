package com.xm.auth.domain.vo;

import lombok.Data;

@Data
public class TokenVo {
    String accessToken;
    String refreshToken;
}
