package com.xm.util.excel.params;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class RowColumnSpan implements Serializable {
    private Integer rowspan;
    private Integer colspan;

    public RowColumnSpan(Integer rowspan, Integer colspan) {
        this.rowspan = rowspan;
        this.colspan = colspan;
    }
}
