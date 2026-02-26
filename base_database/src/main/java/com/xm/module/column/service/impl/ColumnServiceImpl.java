package com.xm.module.column.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.annotation.IgnoreAuth;
import com.xm.auth.domain.entity.TcUser;
import com.xm.module.column.domain.params.ColumnConfig;
import com.xm.core.params.ColumnProps;
import com.xm.module.column.service.ColumnService;
import com.xm.module.config.domain.entity.TcConfig;
import com.xm.core.consts.ColumnPropsType;
import com.xm.util.config.ConfigUtil;
import com.xm.util.auth.UserInfoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColumnServiceImpl implements ColumnService {

    private final String databaseConfigCode = "columnConfig";

    private String getGlobalConfigCode(){
        return databaseConfigCode;
    }

    private String getUserConfigCode(){
        TcUser loginUserBySessionNotNull = UserInfoUtil.getCurrentLoginUserBySessionOrTokenNotNull();
        return StrUtil.format("{}_{}",databaseConfigCode,loginUserBySessionNotNull.getUsername());
    }


    private List<ColumnProps> handleColumnUpdate(List<ColumnProps> defaultColumns, List<ColumnProps> dataBaseColumns){
        if (CollectionUtil.isEmpty(defaultColumns)){
            defaultColumns=new ArrayList<>();
        }
        if (CollectionUtil.isEmpty(dataBaseColumns)){
            dataBaseColumns=new ArrayList<>();
        }
        List<ColumnProps> finalDefaultColumns = defaultColumns;
        //获取默认配置prop序列
        Map<String, Integer> defaultColumnsPropIndexMap = IntStream.
                range(0, defaultColumns.size()).boxed()
                .collect(Collectors.toMap(index -> finalDefaultColumns.get(index).getProp(), index -> index));
        //获取数据库配置prop序列
        Map<String, ColumnProps> defaultColumnsPropMap = defaultColumns.stream().collect(Collectors.toMap(ColumnProps::getProp, Function.identity()));
        Map<String, ColumnProps> dataBaseColumnsPropMap = dataBaseColumns.stream().collect(Collectors.toMap(ColumnProps::getProp, Function.identity()));
        //默认配置对数据库配置取差集就是新增
        List<String> newAddColumn = defaultColumnsPropMap.keySet().stream()
                .filter(item -> !dataBaseColumnsPropMap.containsKey(item)).collect(Collectors.toList());
        //数据库配置对默认配置取差集就是删除
        List<String> deleteColumn = dataBaseColumnsPropMap.keySet().stream()
                .filter(item -> !defaultColumnsPropMap.containsKey(item)).collect(Collectors.toList());
        /*
         * 以数据库为基底
         */
        //处理删除
        List<ColumnProps> databaseList =
                dataBaseColumns.stream().filter(item -> !deleteColumn.contains(item.getProp())).collect(Collectors.toList());
        // 创建结果映射，容量为最终列表大小
        Map<Integer, ColumnProps> resultMap = new HashMap<>(databaseList.size() + newAddColumn.size());
        // 处理新增列：按默认顺序放入指定位置
        newAddColumn.stream()
                .map(defaultColumnsPropMap::get)
                .filter(Objects::nonNull)
                .forEach(columnProps -> {
                    Integer index = defaultColumnsPropIndexMap.get(columnProps.getProp());
                    if (index != null) {
                        resultMap.put(index, columnProps);
                    }
                });
        // 找出未被占用的索引位置
        List<Integer> availableIndexes = IntStream.range(0, databaseList.size() + newAddColumn.size())
                .filter(i -> !resultMap.containsKey(i))
                .boxed()
                .sorted()
                .collect(Collectors.toList());
        // 将数据库中的列按顺序填入空闲位置
        Iterator<Integer> indexIterator = availableIndexes.iterator();
        databaseList.forEach(column -> {
            if (indexIterator.hasNext()) {
                resultMap.put(indexIterator.next(), column);
            }
        });
        // 按索引排序生成最终列表
        List<ColumnProps> list = resultMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        //以数据库为基底处理字段类型变更或者名称变更或子项变更
        for (ColumnProps dataBaseColumnProps:list){
            ColumnProps defaultColumn = defaultColumnsPropMap.get(dataBaseColumnProps.getProp());
            if (defaultColumn==null){
                continue;
            }
            //名称类型
            dataBaseColumnProps.setType(defaultColumn.getType());
            dataBaseColumnProps.setLabel(defaultColumn.getLabel());
            //子项
            if (ColumnPropsType.LEVEL.equals(defaultColumn.getType())){
                List<ColumnProps> defaultChildren = defaultColumn.getChildren();
                List<ColumnProps> databaseChildren = dataBaseColumnProps.getChildren();
                List<ColumnProps> childrenList = handleColumnUpdate(defaultChildren, databaseChildren);
                dataBaseColumnProps.setChildren(childrenList);
            }
        }

        return list;
    }

    @Override
    public List<ColumnProps> getColumnPropsConfig(ColumnConfig columnConfig) {
        List<ColumnProps> defaultColumns = columnConfig.getDefaultColumns();
        String groupName = columnConfig.getGroupName();
        Boolean global = columnConfig.getGlobal();
        if (CollectionUtil.isEmpty(defaultColumns)){
            return new ArrayList<>();
        }
        if (!UserInfoUtil.isLogin()){
            return defaultColumns;
        }
        if (StrUtil.isBlank(groupName)){
            throw new CommonException("列配置groupName不能为空");
        }
        TcConfig config;
        if (global){
            config = ConfigUtil.getConfig(groupName, getGlobalConfigCode());
        }else {
            config = ConfigUtil.getConfig(groupName, getUserConfigCode());
        }
        if (config==null){
            return defaultColumns;
        }else {
            List<ColumnProps> dataBaseColumns = JSONUtil.toList(config.getConfigValue(), ColumnProps.class);
            return handleColumnUpdate(defaultColumns,dataBaseColumns);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveColumnPropsConfig(ColumnConfig columnConfig) {
        if (!UserInfoUtil.isLogin()){
            throw new CommonException("未登录,无法配置");
        }
        List<ColumnProps> defaultColumns = columnConfig.getDefaultColumns();
        String groupName = columnConfig.getGroupName();
        Boolean global = columnConfig.getGlobal();
        if (CollectionUtil.isEmpty(defaultColumns)){
            throw new CommonException("列配置数据不能为空");
        }
        if (StrUtil.isBlank(groupName)){
            throw new CommonException("列配置groupName不能为空");
        }
        if (global){
            ConfigUtil.saveConfig(groupName,getGlobalConfigCode(),JSONUtil.toJsonStr(defaultColumns));
        }else {
            ConfigUtil.saveConfig(groupName,getUserConfigCode(),JSONUtil.toJsonStr(defaultColumns));
        }
        return "操作成功";
    }

    @Override
    public String resetColumnPropsConfig(ColumnConfig columnConfig) {
        if (!UserInfoUtil.isLogin()){
            throw new CommonException("未登录,无法配置");
        }
        String groupName = columnConfig.getGroupName();
        Boolean global = columnConfig.getGlobal();
        if (StrUtil.isBlank(groupName)){
            throw new CommonException("列配置groupName不能为空");
        }
        if (global){
            ConfigUtil.deleteConfig(groupName,getGlobalConfigCode());
        }else {
            ConfigUtil.deleteConfig(groupName,getUserConfigCode());
        }
        return "操作成功";
    }
}
