package com.xm.util.excel.params;

import lombok.Data;

@Data
public class DefineExcelMapping {
    private String tittle;
    private Class fieldClass;
    private String fieldName;

    public DefineExcelMapping(String tittle, Class fieldClass, String fieldName) {
        this.tittle = tittle;
        this.fieldClass = fieldClass;
        this.fieldName = fieldName;
    }
}
