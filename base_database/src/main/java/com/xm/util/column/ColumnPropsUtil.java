package com.xm.util.column;

import cn.hutool.core.collection.CollectionUtil;
import com.xm.core.params.ColumnProps;

import java.util.List;
import java.util.Map;

public class ColumnPropsUtil {

    public final static String greenClassName="bg-green";
    public final static String yellowClassName="bg-yellow";
    public final static String redClassName="bg-red";


    public static int handlePropAndColumnIndexMapRecursion(List<ColumnProps> columnPropsList, Integer startIndex, Map<String,Integer> propAndColumnIndexMap){
        if (CollectionUtil.isEmpty(columnPropsList)){
            return startIndex;
        }
        for (ColumnProps columnProps : columnPropsList) {
            if (CollectionUtil.isEmpty(columnProps.getChildren())) {
                //不含子项直接映射处理
                propAndColumnIndexMap.put(columnProps.getProp(), startIndex);
                startIndex++;
            } else {
                //带子项的递归处理
                startIndex = handlePropAndColumnIndexMapRecursion(columnProps.getChildren(), startIndex, propAndColumnIndexMap);
            }
        }
        return startIndex;
    }
}
