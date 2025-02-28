package com.xm.systemFront.domain.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class TcSystemFrontToken implements Serializable {
    /**
    * token
    */
    @Size(max = 100,message = "token最大长度要小于 100")
    @NotBlank(message = "token不能为空")
    private String accessToken;

    /**
    * 名称
    */
    @Size(max = 50,message = "名称最大长度要小于 50")
    @NotBlank(message = "名称不能为空")
    private String name;

    private static final long serialVersionUID = 1L;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken == null ? null : accessToken.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }
}