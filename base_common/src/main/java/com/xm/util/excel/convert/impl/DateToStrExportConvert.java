package com.xm.util.excel.convert.impl;

import cn.hutool.core.date.DateUtil;
import com.xm.util.excel.params.ModelAndTitleExcelMapping;
import com.xm.util.excel.convert.ExportConvert;

import java.util.Date;

public class DateToStrExportConvert implements ExportConvert<String,Date> {

    private String format;

    public static final String DATE_FORMAT="yyyy-MM-dd";

    public DateToStrExportConvert() {
        this.format=DATE_FORMAT;
    }

    public DateToStrExportConvert(String format) {
        this.format = format;
    }

    @Override
    public String convert(Date value, ModelAndTitleExcelMapping mapping) {
        if (value==null){
            return "";
        }
        return DateUtil.format(value,this.format);
    }
}
