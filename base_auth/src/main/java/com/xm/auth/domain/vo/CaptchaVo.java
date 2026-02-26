package com.xm.auth.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CaptchaVo implements Serializable {
    private String base64;
}
