package com.xm.util.columnProp;

import cn.hutool.core.collection.CollectionUtil;
import com.xm.core.params.ColumnProps;

import java.util.ArrayList;
import java.util.List;

public class ColumnPropUtil {

   //把level类型拍成一级
   public static List<ColumnProps> sinkColumnPropsLevel(List<ColumnProps> columnPropsList) {
        List<ColumnProps> resultList = new ArrayList<>();
        if (CollectionUtil.isEmpty(columnPropsList)){
            return resultList;
        }
        for (ColumnProps columnProps : columnPropsList){
            if (CollectionUtil.isEmpty(columnProps.getChildren())){
                resultList.add(columnProps);
            }else {
                resultList.addAll(sinkColumnPropsLevel(columnProps.getChildren()));
            }
        }
        return resultList;
    }
}
