package com.xm.auth.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class RouterVo {
    private String path;

    private String name;

    private String component;

    private String redirect;

    private Boolean fullscreen;

    private RouteMeta meta;

    private List<RouterVo> children;
}
