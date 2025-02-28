package com.xm.util.excel.convert.impl;

import com.xm.util.excel.params.ModelAndTitleExcelMapping;
import com.xm.util.excel.convert.ImportConvert;

public class  BaseToStrImportConvert implements ImportConvert<Object,String> {

    @Override
    public String convert(Object value, ModelAndTitleExcelMapping mapping) {
        return value!=null?String.valueOf(value):null;
    }
}
