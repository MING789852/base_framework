package com.xm.util.auth.params;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.io.Serializable;

@Data
public class RequestAction implements Serializable {
    private String url;
    private String actionTime;


    public RequestAction(){
    }

    public RequestAction(String url, String actionTime) {
        this.url = url;
        this.actionTime = actionTime;
    }

    @Override
    public String toString() {
        return StrUtil.format("【{}】{}",actionTime,url);
    }
}
