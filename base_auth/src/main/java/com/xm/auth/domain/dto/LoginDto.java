package com.xm.auth.domain.dto;

import lombok.Data;

@Data
public class LoginDto {
    private String username;
    private String password;
    private String captchaCode;
}
