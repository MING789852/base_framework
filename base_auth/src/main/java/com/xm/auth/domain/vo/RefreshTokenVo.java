package com.xm.auth.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class RefreshTokenVo implements Serializable {
    private String accessToken;
    private String refreshToken;
    @JsonFormat
    private Date expires;
}
