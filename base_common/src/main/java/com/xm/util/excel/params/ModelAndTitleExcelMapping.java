package com.xm.util.excel.params;

import com.xm.util.excel.convert.ExportConvert;
import com.xm.util.excel.convert.ImportConvert;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.List;

@Data
public class  ModelAndTitleExcelMapping {
    private String title;
    private String key;
    private Integer width;
    private boolean isFileColumn=false;
    private List<ModelAndTitleExcelMapping> children;
    private String type;
    private ImportConvert<?,?> importConvert;
    private ExportConvert<?,?> exportConvert;

    public ModelAndTitleExcelMapping(){
    }

    public ModelAndTitleExcelMapping(String title, String key){
        this.title = title;
        this.key = key;
    }

    public ModelAndTitleExcelMapping(String title, String key,ExportConvert<?,?> exportConvert){
        this.title = title;
        this.key = key;
        this.exportConvert = exportConvert;
    }

    public ModelAndTitleExcelMapping(String title, String key,ImportConvert<?,?> importConvert){
        this.title = title;
        this.key = key;
        this.importConvert = importConvert;
    }
}
