package com.xm.auth.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RouterVo implements Serializable {
    private String path;

    private String name;

    private String component;

    private String redirect;

    private Boolean fullscreen;

    private RouteMeta meta;

    private List<RouterVo> children;
}
