package com.xm.auth.domain.vo;

import lombok.Data;

@Data
public class RouteMeta {
    private String title;

    private String icon;

    private boolean showLink;

    private boolean showParent;

    private boolean keepAlive;

    private boolean hiddenTag;

    private Integer rank;
}
