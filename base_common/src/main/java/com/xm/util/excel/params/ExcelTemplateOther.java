package com.xm.util.excel.params;

import com.xm.util.excel.params.img.ExcelImgIndexInterface;
import lombok.Data;

import java.util.Map;

@Data
public class ExcelTemplateOther {
    private Map<String, ExcelImgIndexInterface> excelTemplateImgInterfaceMap;
}
