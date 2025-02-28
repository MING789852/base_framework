package com.xm.util.excel.convert.impl;


import com.xm.core.enums.IBaseEnum;
import com.xm.util.excel.params.ModelAndTitleExcelMapping;
import com.xm.util.excel.convert.ImportConvert;

public class  DictLabelImportConvert<R,E extends Enum<E> & IBaseEnum<R>> implements ImportConvert<String,R> {
    private final Class<E>  eClass;

    public DictLabelImportConvert(Class<E> eClass) {
        this.eClass = eClass;
    }

    @Override
    public R convert(String value, ModelAndTitleExcelMapping mapping) {
        return (R) IBaseEnum.getValueByLabel(value,eClass);
    }
}
