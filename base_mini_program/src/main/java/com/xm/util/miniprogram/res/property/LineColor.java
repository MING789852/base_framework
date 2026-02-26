package com.xm.util.miniprogram.res.property;

import lombok.Data;

@Data
public class LineColor {
    private int r;
    private int g;
    private int b;

    public LineColor(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
