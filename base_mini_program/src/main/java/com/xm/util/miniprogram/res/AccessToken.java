package com.xm.util.miniprogram.res;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AccessToken {
    private String access_token;
    private int expires_in;
    private int errcode;
    private String errmsg;
}
