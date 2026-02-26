package com.xm.module.core.params;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.enums.QueryConditionEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
public class QueryData<T>{
    private long size=10;
    private long current=1;

    private Map<String,Object> queryParams=new HashMap<>();

    //value为排序类型(descending、ascending)
    //key为排序字段
    private Map<String,Object> orderByParams=new HashMap<>();

    private QueryWrapper<T> wrapper=new QueryWrapper<>();

    private void initLikeIn(List<String> list,String property){
        if (CollectionUtil.isNotEmpty(list)) {
            if (list.size()==1){
                String s = list.get(0);
                wrapper.like(property, s);
            }else {
                for (int i=0;i<list.size();i++){
                    if (i==0){
                        String s = list.get(i);
                        wrapper.like(property, s);
                    }else{
                        String s = list.get(i);
                        wrapper.or().like(property,s);
                    }
                }
            }
        }
    }

    private void convertToWrapper(String property,String condition,Object value){
        if (QueryConditionEnum.EQ.getValue().equals(condition)){
            if (value instanceof String) {
                if (StrUtil.isBlank(value.toString())) {
                    return;
                }
            }
            wrapper.eq(property, value);
        }
        if (QueryConditionEnum.LIKE.getValue().equals(condition)){
            if (value instanceof String) {
                if (StrUtil.isNotBlank(value.toString())) {
                    wrapper.like(property, value);
                }
            }
        }
        if (QueryConditionEnum.NOT_IN.getValue().equals(condition)){
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (CollectionUtil.isNotEmpty(list)) {
                    wrapper.notIn(property, list);
                }
            }
        }
        if (QueryConditionEnum.IN.getValue().equals(condition)){
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (CollectionUtil.isNotEmpty(list)) {
                    wrapper.in(property, list);
                }
            }
        }
        if (QueryConditionEnum.LIKE_IN.getValue().equals(condition)){
            if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> list = (List<String>) value;
                initLikeIn(list,property);
            }
            if (value instanceof String){
                String query = value.toString();
                List<String> list = Arrays.stream(query.split(",")).collect(Collectors.toList());
                initLikeIn(list,property);
            }
        }
        if (QueryConditionEnum.RANGE.getValue().equals(condition)){
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (CollectionUtil.isNotEmpty(list)) {
                    if (list.size() != 2) {
                        throw new CommonException("between条件错误");
                    }
                    Object start = list.get(0);
                    Object end = list.get(1);
                    if (start!=null){
                        if (StrUtil.isNotBlank(start.toString())){
                            wrapper.ge(property,start);
                        }
                    }
                    if (end!=null){
                        if (StrUtil.isNotBlank(end.toString())){
                            wrapper.le(property,end);
                        }
                    }
                }
            }
        }
        if (QueryConditionEnum.BETWEEN_DATE_STR.getValue().equals(condition)){
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (CollectionUtil.isNotEmpty(list)) {
                    if (list.size() != 2) {
                        throw new CommonException("between条件错误");
                    }
                    Object start = list.get(0);
                    Object end = list.get(1);
                    if (start!=null){
                        if (start instanceof Date){
                            Date startDate= (Date) start;
                            start=DateUtil.format(startDate,"yyyy-MM-dd");
                        }
                        if (start instanceof String){
                            try {
                                Date startDate=DateUtil.parse(start.toString(),"yyyy-MM-dd");
                                start=DateUtil.format(startDate,"yyyy-MM-dd");
                            }catch (Exception e){
                                log.error("start转换日期字符串错误",e);
                            }
                        }
                    }
                    if (end!=null){
                        if (end instanceof Date){
                            Date endDate= (Date) end;
                            end=DateUtil.format(endDate,"yyyy-MM");
                        }
                        if (end instanceof String){
                            try {
                                Date endDate=DateUtil.parse(end.toString(),"yyyy-MM-dd");
                                end=DateUtil.format(endDate,"yyyy-MM-dd");
                            }catch (Exception e){
                                log.error("end转换日期字符串错误",e);
                            }
                        }
                    }
                    wrapper.between(property, start, end);
                }
            }
        }
        if (QueryConditionEnum.BETWEEN_MONTH_STR.getValue().equals(condition)){
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (CollectionUtil.isNotEmpty(list)) {
                    if (list.size() != 2) {
                        throw new CommonException("between条件错误");
                    }
                    Object start = list.get(0);
                    Object end = list.get(1);
                    if (start!=null){
                        if (start instanceof Date){
                            Date startDate= (Date) start;
                            start=DateUtil.format(startDate,"yyyy-MM");
                        }
                        if (start instanceof String){
                            try {
                                Date startDate=DateUtil.parse(start.toString(),"yyyy-MM-dd");
                                start=DateUtil.format(startDate,"yyyy-MM");
                            }catch (Exception e){
                                log.error("start转换月份字符串错误",e);
                            }
                        }
                    }
                    if (end!=null){
                        if (end instanceof Date){
                            Date endDate= (Date) end;
                            end=DateUtil.format(endDate,"yyyy-MM");
                        }
                        if (end instanceof String){
                            try {
                                Date endDate=DateUtil.parse(end.toString(),"yyyy-MM-dd");
                                end=DateUtil.format(endDate,"yyyy-MM");
                            }catch (Exception e){
                                log.error("end转换月份字符串错误",e);
                            }
                        }
                    }
                    wrapper.between(property, start, end);
                }
            }
        }
        if (QueryConditionEnum.BETWEEN_MONTH.getValue().equals(condition)){
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (CollectionUtil.isNotEmpty(list)) {
                    if (list.size() != 2) {
                        throw new CommonException("between条件错误");
                    }
                    Object start = list.get(0);
                    Object end = list.get(1);
                    if (start!=null){
                        if (start instanceof Date){
                            Date startDate= (Date) start;
                            start=DateUtil.beginOfMonth(startDate);
                        }
                        if (start instanceof String){
                            try {
                                Date startDate=DateUtil.parse(start.toString(),"yyyy-MM-dd");
                                start=DateUtil.beginOfMonth(startDate);
                            }catch (Exception e){
                                log.error("start转换月份错误",e);
                            }
                        }
                    }
                    if (end!=null){
                        if (end instanceof Date){
                            Date endDate= (Date) end;
                            end=DateUtil.endOfMonth(endDate);
                        }
                        if (end instanceof String){
                            try {
                                Date endDate=DateUtil.parse(end.toString(),"yyyy-MM-dd");
                                end=DateUtil.endOfMonth(endDate);
                            }catch (Exception e){
                                log.error("end转换月份错误",e);
                            }
                        }
                    }
                    wrapper.between(property, start, end);
                }
            }
        }
        if (QueryConditionEnum.BETWEEN_DATE.getValue().equals(condition)){
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (CollectionUtil.isNotEmpty(list)) {
                    if (list.size() != 2) {
                        throw new CommonException("between条件错误");
                    }
                    Object start = list.get(0);
                    Object end = list.get(1);
                    if (start!=null){
                        if (start instanceof Date){
                            Date startDate= (Date) start;
                            start=DateUtil.beginOfDay(startDate);
                        }
                        if (start instanceof String){
                            try {
                                Date startDate=DateUtil.parse(start.toString(),"yyyy-MM-dd");
                                start=DateUtil.beginOfDay(startDate);
                            }catch (Exception e){
                                log.error("start转换日期错误",e);
                            }
                        }
                    }
                    if (end!=null){
                        if (end instanceof Date){
                            Date endDate= (Date) end;
                            end=DateUtil.endOfDay(endDate);
                        }
                        if (end instanceof String){
                            try {
                                Date endDate=DateUtil.parse(end.toString(),"yyyy-MM-dd");
                                end=DateUtil.endOfDay(endDate);
                            }catch (Exception e){
                                log.error("end转换日期错误",e);
                            }
                        }
                    }
                    wrapper.between(property, start, end);
                }
            }
        }
    }

    /**
     * 生成QueryWrapper(动态字段查询条件生成)
     */
    public QueryWrapper<T> generateQueryWrapperWithDynamic(){
//        QueryWrapper<T> queryWrapper=new QueryWrapper<>();
        if (CollectionUtil.isEmpty(queryParams)){
            return wrapper;
        }
        //处理参数
        for (Map.Entry<String,Object> entry:queryParams.entrySet()){
            Object value=entry.getValue();
            // 查询参数判空
            if (value==null){
                continue;
            }
            //拆分参数
            String [] propertyList=entry.getKey().split("_\\$_");
            if (!ArrayUtil.isEmpty(propertyList)&&propertyList.length==2){
                String property;
                if (propertyList[0].startsWith("dynamic_")){
                    property=propertyList[0];
                }else {
                    // 驼峰转下划线
                    property=StrUtil.toUnderlineCase(propertyList[0]);
                }
                // 获取查询条件
                String condition= propertyList[1];
                convertToWrapper(property,condition,value);
            }
        }
        return wrapper;
    }

    /**
     * 生成QueryWrapper(禁用驼峰转下划线)
     */
    public QueryWrapper<T> generateQueryWrapperDisUnderlineCase(){
        //处理参数
        if (CollectionUtil.isNotEmpty(queryParams)){
            for (Map.Entry<String,Object> entry:queryParams.entrySet()){
                Object value=entry.getValue();
                // 查询参数判空
                if (value==null){
                    continue;
                }
                //拆分参数
                String [] propertyList=entry.getKey().split("_\\$_");
                if (!ArrayUtil.isEmpty(propertyList)&&propertyList.length==2){
                    // 驼峰转下划线
                    String property=propertyList[0];
                    // 获取查询条件
                    String condition= propertyList[1];
                    convertToWrapper(property,condition,value);
                }
            }
        }
        //处理排序字段
        if (CollectionUtil.isNotEmpty(orderByParams)){
            for (Map.Entry<String,Object> entry:orderByParams.entrySet()){
                Object orderTypeObj=entry.getValue();
                String property=entry.getKey();
                // 查询参数判空
                if (orderTypeObj==null){
                    continue;
                }
                String orderType=orderTypeObj.toString();
                if ("ascending".equals(orderType)){
                    wrapper.orderByAsc(property);
                }else {
                    wrapper.orderByDesc(property);
                }
            }
        }
        return wrapper;
    }

    /**
     * 生成QueryWrapper(驼峰转下划线)
     */
    public QueryWrapper<T> generateQueryWrapper(){
        //处理查询参数
        if (CollectionUtil.isNotEmpty(queryParams)){
            for (Map.Entry<String,Object> entry:queryParams.entrySet()){
                Object value=entry.getValue();
                // 查询参数判空
                if (value==null){
                    continue;
                }
                //拆分参数
                String [] propertyList=entry.getKey().split("_\\$_");
                if (!ArrayUtil.isEmpty(propertyList)&&propertyList.length==2){
                    // 驼峰转下划线
                    String property=StrUtil.toUnderlineCase(propertyList[0]);
                    // 获取查询条件
                    String condition= propertyList[1];
                    convertToWrapper(property,condition,value);
                }
            }
        }
        //处理排序字段
        if (CollectionUtil.isNotEmpty(orderByParams)){
            for (Map.Entry<String,Object> entry:orderByParams.entrySet()){
                Object orderTypeObj=entry.getValue();
                String property=StrUtil.toUnderlineCase(entry.getKey());
                // 查询参数判空
                if (orderTypeObj==null){
                    continue;
                }
                String orderType=orderTypeObj.toString();
                if ("ascending".equals(orderType)){
                    wrapper.orderByAsc(property);
                }else {
                    wrapper.orderByDesc(property);
                }
            }
        }
        return wrapper;
    }
}
