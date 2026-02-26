package com.xm.util.excel.params;


import com.xm.core.params.ColumnProps;
import com.xm.util.excel.ExcelUtils;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Data
public class DynamicExcelWithMerge<T> {
    //二选一，优先使用mappingList
    private List<ModelAndTitleExcelMapping> mappingList;
    private List<ColumnProps> mainColumns;
    //主表数据
    private List<Map<String,T>> mainTableData;
    //key: row_column 例如 1_3
    private Map<String, RowColumnSpan> rowColumnSpanMap;
    //表标题
    private String title;
    //决定是否为同一个表
    private String sheetName= ExcelUtils.sheetDefaultName;

    private Function<List<ModelAndTitleExcelMapping>, List<ModelAndTitleExcelMapping>> handleMapping;

    private ExcelOtherInfo excelOtherInfo;
}
