package com.xm.util.excel.convert;

import com.xm.util.excel.params.ModelAndTitleExcelMapping;

public interface ExportConvert <R,V> {
    R convert(V value, ModelAndTitleExcelMapping mapping);
}

