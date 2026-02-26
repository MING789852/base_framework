package com.xm.util.auth.params;

import lombok.Data;

@Data
public class LoginTry {
    private Integer count=0;
    private String lastTryTime;
}
