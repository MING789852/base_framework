package com.xm.core.params;

import lombok.Data;

@Data
public class JumpResultPage {
    private Integer code;
    private Result<Object> data;

    public JumpResultPage(Result<Object> data) {
        this.data = data;
        this.code = 9999;
    }

    public static JumpResultPage success(Object data) {
        return new JumpResultPage(Result.successForData(data));
    }

    public static JumpResultPage error(String msg) {
        return new JumpResultPage(Result.error(msg));
    }
}
