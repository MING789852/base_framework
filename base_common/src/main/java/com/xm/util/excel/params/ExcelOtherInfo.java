package com.xm.util.excel.params;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExcelOtherInfo {
    //外层Map的key值为Excel表列对应（一般为fieldKey）
    //内层Map的key值为数据列的行对应（从0开始）
    private Map<String, Map<String,List<ExcelFile>>> fileListMapping;
}
