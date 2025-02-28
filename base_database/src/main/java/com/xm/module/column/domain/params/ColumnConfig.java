package com.xm.module.column.domain.params;

import com.xm.core.params.ColumnProps;
import lombok.Data;

import java.util.List;

@Data
public class ColumnConfig {
    private List<ColumnProps> defaultColumns;
    private String groupName;
    private Boolean global;
}
