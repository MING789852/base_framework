package com.xm.util.excel.params.img;

import com.xm.util.excel.params.MergedInfo;
import lombok.Data;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Map;

@Data
public class ExcelImgIndexParams {
    private MergedInfo mergedInfo;
    private Cell cell;
    private Workbook workbook;
    private Sheet sheet;
    private Map<String, Object> dataMap;
    private Integer startColumn;
    private Integer startRow;
    private Integer endColumn;
    private Integer endRow;
    private Integer allWidth;
    private Integer allHeight;
}
