package com.xm.flowable.enums;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.flowable.domain.dto.VariableData;

import java.math.BigDecimal;

public class VariableDataTypeEnum {
    //BigDecimal类型
    public final static String BigDecimal_type="BigDecimal";
    //String类型
    public final static String String_type="String";
    //Integer类型
    public final static String Integer_type= "Integer";
    //Boolean类型
    public final static String Boolean_type="Boolean";
    //Double类型
    public final static String Double_type="Double";
    //Float类型
    public final static String Float_type="Float";
    //Long类型
    public final static String Long_type="Long";
    //ListJson类型
    public final static String ListJson="ListJson";

    public static Object convertStrValueToObjValue(VariableData variableData){
        if (variableData!=null){
            String type = variableData.getType();
            if (type==null){
                throw new CommonException(StrUtil.format("请指定变量->{}类型", variableData.getKey()));
            }
            if (VariableDataTypeEnum.BigDecimal_type.equals(type)){
                return new BigDecimal(variableData.getValue().toString());
            }
            if (VariableDataTypeEnum.String_type.equals(type)){
                return variableData.getValue().toString();
            }
            if (VariableDataTypeEnum.Integer_type.equals(type)){
                return Integer.parseInt(variableData.getValue().toString());
            }
            if (VariableDataTypeEnum.Boolean_type.equals(type)){
                return Boolean.parseBoolean(variableData.getValue().toString());
            }
            if (VariableDataTypeEnum.Double_type.equals(type)){
                return Double.parseDouble(variableData.getValue().toString());
            }
            if (VariableDataTypeEnum.Float_type.equals(type)){
                return Float.parseFloat(variableData.getValue().toString());
            }
            if (VariableDataTypeEnum.Long_type.equals(type)){
                return Long.parseLong(variableData.getValue().toString());
            }
            if (VariableDataTypeEnum.ListJson.equals(type)){
                return JSONUtil.toList(variableData.getValue().toString(), Object.class);
            }
            return variableData.getValue();
        }else {
            return null;
        }
    }
}
