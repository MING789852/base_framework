package com.xm.status.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.form.service.TcFormMainModelService;
import com.xm.status.domain.entity.TcStatusDetailModel;
import com.xm.status.mapper.TcStatusDetailModelMapper;
import com.xm.status.service.TcStatusDetailModelService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.valid.ValidationUtils;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcStatusDetailModelServiceImpl implements TcStatusDetailModelService {

    private final TcStatusDetailModelMapper detailModelMapper;

    private final TcFormMainModelService formMainModelService;

    @Override
    public TcStatusDetailModelMapper getMapper() {
        return detailModelMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteData(List<TcStatusDetailModel> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空");
        }
        List<String> idList = list.stream().map(TcStatusDetailModel::getId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        LambdaQueryWrapper<TcStatusDetailModel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(TcStatusDetailModel::getId,idList);
        detailModelMapper.delete(lambdaQueryWrapper);
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateData(List<TcStatusDetailModel> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空");
        }
        Date now=new Date();
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，无权限操作");
        }
        ValidationUtils.validateEntityListIgnore(list, Collections.singletonList("id"));
        for (TcStatusDetailModel detailModel:list){
            if (StrUtil.isBlank(detailModel.getId())){
                detailModel.setId(SnowIdUtil.getSnowId());
                detailModel.setCreateDate(now);
                detailModel.setCreateUser(currentLoginUserBySession.getNickName());
                detailModelMapper.insert(detailModel);
            }else {
                detailModel.setUpdateDate(now);
                detailModel.setUpdateUser(currentLoginUserBySession.getNickName());
                detailModelMapper.updateById(detailModel);
            }
        }
        return "操作成功";
    }

    @Override
    public Map<String, Object> getDictMapping() {
        Map<String, String> formMainModelIdAndNameMapping = formMainModelService.getFormMainModelIdAndNameMapping();
        Map<String,Object> result=new HashMap<>();
        result.put("formMainModelId",formMainModelIdAndNameMapping);
        return result;
    }

    @Override
    public Map<String, String> getDetailModelMappingByMainModelId(String mainModelId) {
        if (StrUtil.isBlank(mainModelId)){
            throw new CommonException("状态模型ID为空,无法获取状态明细映射");
        }
        LambdaQueryWrapper<TcStatusDetailModel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcStatusDetailModel::getStatusMainModelId,mainModelId);
        List<TcStatusDetailModel> tcStatusDetailModelList = detailModelMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtil.isEmpty(tcStatusDetailModelList)){
            return new HashMap<>();
        }else {
            return tcStatusDetailModelList.stream().collect(Collectors.toMap(TcStatusDetailModel::getId,TcStatusDetailModel::getName));
        }
    }

    @Override
    public List<TcStatusDetailModel> getDetailModelListByMainModelId(String mainModelId) {
        if (StrUtil.isBlank(mainModelId)){
            throw new CommonException("状态模型ID为空,无法获取状态明细映射");
        }
        LambdaQueryWrapper<TcStatusDetailModel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcStatusDetailModel::getStatusMainModelId,mainModelId);
        return detailModelMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public TcStatusDetailModel getStatusDetailModelById(String id) {
        return detailModelMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByMainModelId(String mainModelId) {
        LambdaQueryWrapper<TcStatusDetailModel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcStatusDetailModel::getStatusMainModelId,mainModelId);
        detailModelMapper.delete(lambdaQueryWrapper);
    }
}
