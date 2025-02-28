package com.xm.util.excel.convert;

import com.xm.util.excel.params.ModelAndTitleExcelMapping;

/**
 * 导入excel时使用
 * @param <V>
 */
public interface  ImportConvert<V,R> {
    R convert(V value, ModelAndTitleExcelMapping mapping);
}
