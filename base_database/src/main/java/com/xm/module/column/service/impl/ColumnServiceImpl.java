package com.xm.module.column.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            defaultColumns=new ArrayList<>();
        }
        Map<String, ColumnProps> defaultColumnsPropMap = defaultColumns.stream().collect(Collectors.toMap(ColumnProps::getProp, Function.identity()));
        Map<String, ColumnProps> dataBaseColumnsPropMap = dataBaseColumns.stream().collect(Collectors.toMap(ColumnProps::getProp, Function.identity()));
        //默认配置对数据库配置取差集就是新增
        List<String> newAddColumn = defaultColumnsPropMap.keySet().stream()
                .filter(item -> !dataBaseColumnsPropMap.containsKey(item)).collect(Collectors.toList());
        //数据库配置对默认配置取差集就是删除
        List<String> deleteColumn = dataBaseColumnsPropMap.keySet().stream()
                .filter(item -> !defaultColumnsPropMap.containsKey(item)).collect(Collectors.toList());
        //以数据库为基底处理删除
        List<ColumnProps> list = dataBaseColumns.stream().filter(item -> !deleteColumn.contains(item.getProp())).collect(Collectors.toList());
        //以数据库为基底处理新增
        for (String add:newAddColumn){
            ColumnProps columnProps = defaultColumnsPropMap.get(add);
            if (columnProps==null){
                continue;
            }
            list.add(columnProps);
        }
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
