package com.xm.util.excel.convert.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.xm.util.excel.params.ModelAndTitleExcelMapping;
import com.xm.util.excel.convert.ImportConvert;

public class DateToStrImportConvert implements ImportConvert<DateTime,String> {

    private final String format;

    public DateToStrImportConvert(String format) {
        this.format = format;
    }

    @Override
    public String convert(DateTime value, ModelAndTitleExcelMapping mapping) {
        return value!=null? DateUtil.format(value,format):null;
    }
}
