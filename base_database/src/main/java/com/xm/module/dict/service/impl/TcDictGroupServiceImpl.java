package com.xm.module.dict.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.advice.exception.exception.CommonException;
import com.xm.module.core.params.QueryData;
import com.xm.module.dict.domain.entity.TcDict;
import com.xm.module.dict.domain.entity.TcDictGroup;
import com.xm.module.dict.mapper.TcDictGroupMapper;
import com.xm.module.dict.service.TcDictGroupService;
import com.xm.module.dict.service.TcDictService;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.valid.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class TcDictGroupServiceImpl implements TcDictGroupService {

    private final TcDictGroupMapper dictGroupMapper;

    private final TcDictService dictService;

    @Override
    public List<TcDictGroup> selectByList(QueryData<TcDictGroup> queryData) {
        QueryWrapper<TcDictGroup> queryWrapper = queryData.generateQueryWrapper();
        queryWrapper.eq("judge_enable",1);
        queryWrapper.orderByDesc("create_date");
        return dictGroupMapper.selectList(queryWrapper);
    }

    @Override
    public Page<TcDictGroup> selectByPage(QueryData<TcDictGroup> queryData) {
        QueryWrapper<TcDictGroup> queryWrapper = queryData.generateQueryWrapper();
        queryWrapper.eq("judge_enable",1);
        queryWrapper.orderByDesc("create_date");
        Page<TcDictGroup> page=new Page<>(queryData.getCurrent(), queryData.getSize());
        return dictGroupMapper.selectPage(page,queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteData(List<TcDictGroup> list) {
        List<String> idList = list.stream().filter(item -> StrUtil.isNotBlank(item.getId())).map(TcDictGroup::getId).collect(Collectors.toList());
        Set<String> groupKeyList=list.stream().filter(item -> StrUtil.isNotBlank(item.getId())).map(TcDictGroup::getGroupKey).collect(Collectors.toSet());
        if (CollectionUtil.isNotEmpty(idList)){
            //删除主表
            LambdaQueryWrapper<TcDictGroup> parentWrapper=new LambdaQueryWrapper<>();
            parentWrapper.in(TcDictGroup::getId,idList);
            TcDictGroup parent=new TcDictGroup();
            parent.setJudgeEnable(0);
            dictGroupMapper.update(parent,parentWrapper);
            //删除子表
            LambdaQueryWrapper<TcDict> itemWrapper=new LambdaQueryWrapper<>();
            itemWrapper.in(TcDict::getGroupKey, groupKeyList);
            TcDict item=new TcDict();
            item.setJudgeEnable(0);
            dictService.getMapper().update(item,itemWrapper);
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateData(List<TcDictGroup> list) {
        //校验
        ValidationUtils.validateEntityListIgnore(list, Arrays.asList("id","judgeEnable"));
        Date now = new Date();
        LambdaQueryWrapper<TcDictGroup> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        for (TcDictGroup item:list){
            item.setJudgeEnable(1);

            //查找是否存在
            lambdaQueryWrapper.clear();
            lambdaQueryWrapper.eq(TcDictGroup::getGroupKey,item.getGroupKey()).eq(TcDictGroup::getJudgeEnable,1);
            List<TcDictGroup> groupList=dictGroupMapper.selectList(lambdaQueryWrapper);

            if (CollectionUtil.isEmpty(groupList)){
                item.setId(SnowIdUtil.getSnowId());
                item.setCreateDate(now);
                dictGroupMapper.insert(item);
            }else {
                if (groupList.size()!=1){
                    throw new CommonException(StrUtil.format("字典组->{}存在多个",item.getGroupKey()));
                }
                item.setId(groupList.get(0).getId());
                item.setUpdateDate(now);
                dictGroupMapper.updateById(item);
            }
        }
        return "操作成功";
    }

}
