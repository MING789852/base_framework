package com.xm.core.params.dictTag;


import lombok.Data;

@Data
public class Tag {
    private String type;
    private TagStyle style;

    public static final String TYPE_INFO="info";
    public static final String TYPE_PRIMARY="primary";
    public static final String TYPE_WARNING="warning";
    public static final String TYPE_SUCCESS="success";
    public static final String TYPE_DANGER="danger";

    public static String getTagDictName(String name){
        return  name+"Tag";
    }

    public Tag(String type) {
        this.type = type;
    }

    public Tag(TagStyle style) {
        this.type = "";
        this.style = style;
    }
}
