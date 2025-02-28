package com.xm.auth.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class RefreshTokenVo {
    private String accessToken;
    private String refreshToken;
    @JsonFormat
    private Date expires;
}
