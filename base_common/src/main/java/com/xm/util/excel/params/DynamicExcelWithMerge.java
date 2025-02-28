package com.xm.util.excel.params;


import com.xm.core.params.ColumnProps;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DynamicExcelWithMerge {
    private List<ColumnProps> mainColumns;
    private List<Map<String,Object>> mainTableData;
    //key: row_column 例如 1_3
    private Map<String, RowColumnSpan> rowColumnSpanMap;
}
