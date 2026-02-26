package com.xm.util.excel.params;

import lombok.Data;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.HashMap;
import java.util.Map;

@Data
public class MergedInfo {
    //合并索引，用于sheet中定位位置
    private int mergedIndex;
    //合并
    private CellRangeAddress cellAddresses;
    //row为局部行，从0开始
    //col为局部列，从0开始
    //合并单元格局部样式映射、行高映射、列宽映射
    //单元格局部样式映射为key=row_col，行高映射key=row,列宽映射key=col
    Map<String, CellStyle> mergedStyleMap;
    Map<Integer, Integer> rowHeightMap;
    Map<Integer, Integer> colWidthMap;
}
