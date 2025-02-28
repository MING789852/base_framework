package com.xm.core.params.dictTag;

import lombok.Data;

@Data
public class TagStyle {
    private String color;
    private String backgroundColor;

    public TagStyle(String color, String backgroundColor) {
        this.color = color;
        this.backgroundColor = backgroundColor;
    }
}
