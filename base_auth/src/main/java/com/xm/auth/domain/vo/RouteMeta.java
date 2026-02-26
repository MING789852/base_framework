package com.xm.auth.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RouteMeta implements Serializable {
    private String title;

    private String icon;

    private boolean showLink;

    private boolean showParent;

    private boolean keepAlive;

    private boolean hiddenTag;

    private Integer rank;
}
