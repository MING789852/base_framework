package com.xm.module.column.service;

import com.xm.module.column.domain.params.ColumnConfig;
import com.xm.core.params.ColumnProps;


import java.util.List;

public interface ColumnService {
    List<ColumnProps> getColumnPropsConfig(ColumnConfig columnConfig);

    String saveColumnPropsConfig(ColumnConfig columnConfig);

    String resetColumnPropsConfig(ColumnConfig columnConfig);
}
