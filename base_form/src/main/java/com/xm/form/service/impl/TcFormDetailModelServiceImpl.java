package com.xm.form.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.module.core.params.QueryData;
import com.xm.module.dict.domain.entity.TcDictGroup;
import com.xm.module.dict.enums.DictQueryTypeEnum;
import com.xm.module.dict.service.TcDictGroupService;
import com.xm.form.domain.entity.TcFormDetailModel;
import com.xm.form.enums.FormFieldTypeEnum;
import com.xm.form.handler.ExternalFormDetailHandler;
import com.xm.form.mapper.TcFormDetailModelMapper;
import com.xm.form.service.TcFormDetailModelService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.valid.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcFormDetailModelServiceImpl implements TcFormDetailModelService {

    private final TcFormDetailModelMapper formDetailModelMapper;

    private final TcDictGroupService dictGroupService;

    private final List<ExternalFormDetailHandler> formDetailHandlerList;

    @Override
    public TcFormDetailModelMapper getMapper() {
        return formDetailModelMapper;
    }

    @Override
    public Page<TcFormDetailModel> selectByPage(QueryData<TcFormDetailModel> queryData) {
        QueryWrapper<TcFormDetailModel> wrapper = queryData.getWrapper();
        wrapper.orderByAsc("sequence");
        return TcFormDetailModelService.super.selectByPage(queryData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteData(List<TcFormDetailModel> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空");
        }
        List<String> idList= list.stream().map(TcFormDetailModel::getId).collect(Collectors.toList());
        LambdaQueryWrapper<TcFormDetailModel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(TcFormDetailModel::getId,idList);
        formDetailModelMapper.delete(lambdaQueryWrapper);
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateData(List<TcFormDetailModel> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空");
        }
        ValidationUtils.validateEntityListIgnore(list, Collections.singletonList("id"));
        Date now=new Date();
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，无权限操作");
        }
        for (TcFormDetailModel detailModel:list){
            if (StrUtil.isBlank(detailModel.getId())){
                detailModel.setId(SnowIdUtil.getSnowId());
                detailModel.setCreateDate(now);
                detailModel.setCreateUser(currentLoginUserBySession.getNickName());
                formDetailModelMapper.insert(detailModel);
            }else {
                detailModel.setUpdateDate(now);
                detailModel.setUpdateUser(currentLoginUserBySession.getNickName());
                formDetailModelMapper.updateById(detailModel);
            }
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByMainIdList(List<String> mainIdList) {
        if (CollectionUtil.isEmpty(mainIdList)){
            return;
        }
        LambdaQueryWrapper<TcFormDetailModel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(TcFormDetailModel::getFormMainModelId,mainIdList);
        formDetailModelMapper.delete(lambdaQueryWrapper);
    }

    @Override
    public List<TcFormDetailModel> getShowDetailDataListByMainModelId(String mainId) {
        if (StrUtil.isBlank(mainId)){
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TcFormDetailModel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TcFormDetailModel::getFormMainModelId,mainId)
                .eq(TcFormDetailModel::getJudgeShow,true)
                .orderByAsc(TcFormDetailModel::getSequence);
        return formDetailModelMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public Map<String, Object> getDictMapping() {
        Map<String,Object> dictMapping=new HashMap<>();
        //设置字段类型
        Map<String,String> fieldType=new HashMap<>();
        for (FormFieldTypeEnum formFieldTypeEnum:FormFieldTypeEnum.values()){
            fieldType.put(formFieldTypeEnum.getValue(),formFieldTypeEnum.getLabel());
        }
        if (CollectionUtil.isNotEmpty(formDetailHandlerList)){
            for (ExternalFormDetailHandler externalFormDetailHandler:formDetailHandlerList){
                Map<String, String> externalFieldType = externalFormDetailHandler.getFieldType();
                fieldType.putAll(externalFieldType);
            }
        }
        dictMapping.put("fieldType",fieldType);
        //设置字典类型
        Map<String,String> dictType=new HashMap<>();
        for (DictQueryTypeEnum dictQueryTypeEnum:DictQueryTypeEnum.values()){
            dictType.put(dictQueryTypeEnum.getValue(), dictQueryTypeEnum.getLabel());
        }
        dictMapping.put("other1",dictType);
        //设置字典组
        List<TcDictGroup> tcDictGroups = dictGroupService.selectByList(new QueryData<>());
        Map<String,String> dictGroup=new HashMap<>();
        if (CollectionUtil.isNotEmpty(tcDictGroups)){
            dictGroup = tcDictGroups.stream().collect(Collectors.toMap(TcDictGroup::getGroupKey, TcDictGroup::getGroupName));
        }
        dictMapping.put("other2",dictGroup);

        Map<Boolean,String> trueOrFalse=new HashMap<>();
        trueOrFalse.put(true,"是");
        trueOrFalse.put(false,"否");
        dictMapping.put("required",trueOrFalse);
        dictMapping.put("judgeShow",trueOrFalse);

        return dictMapping;
    }
}
