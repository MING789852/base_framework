package com.xm.util.miniprogram.res;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class JsCode2Session {

    private String openid;
    private String session_key;
    private String unionid;
    private int errcode;
    private String errmsg;
}
