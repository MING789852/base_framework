package com.xm.util.excel.params;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.io.Serializable;

@Data
public class RowColumnSpan implements Serializable {
    private Integer rowspan;
    private Integer colspan;

    public RowColumnSpan() {
    }

    public RowColumnSpan(Integer rowspan, Integer colspan) {
        this.rowspan = rowspan;
        this.colspan = colspan;
    }

    public static String getKey(Integer rowIndex, Integer colIndex){
        return StrUtil.format("{}_{}",rowIndex,colIndex);
    }
}
