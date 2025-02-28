package com.xm.form.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.form.domain.entity.TcFormDynamicField;
import com.xm.form.mapper.TcFormDynamicFieldMapper;
import com.xm.form.service.TcFormDynamicFieldService;
import com.xm.util.id.SnowIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcFormDynamicFieldServiceImpl implements TcFormDynamicFieldService {

    private final TcFormDynamicFieldMapper formDynamicFieldMapper;

    @Override
    public TcFormDynamicFieldMapper getMapper() {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDynamicField(TcFormDynamicField dynamicField) {
        String formInsId = dynamicField.getFormInsId();
        String formDetailId = dynamicField.getFormDetailId();
        TcFormDynamicField dataBase=getDynamicField(formInsId,formDetailId);
        if (dataBase==null){
            dynamicField.setId(SnowIdUtil.getSnowId());
            formDynamicFieldMapper.insert(dynamicField);
        }else {
            LambdaQueryWrapper<TcFormDynamicField> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper
                    .eq(TcFormDynamicField::getFormDetailId,formDetailId)
                    .eq(TcFormDynamicField::getFormInsId,formInsId);
            formDynamicFieldMapper.update(dynamicField,lambdaQueryWrapper);
        }
    }

    @Override
    public TcFormDynamicField getDynamicField(String formInsId, String formDetailId) {
        if (StrUtil.isBlank(formDetailId)||StrUtil.isBlank(formInsId)){
            return null;
        }
        LambdaQueryWrapper<TcFormDynamicField> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TcFormDynamicField::getFormDetailId,formDetailId)
                .eq(TcFormDynamicField::getFormInsId,formInsId);
        return formDynamicFieldMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByFormInsIdList(List<String> idList) {
        if (CollectionUtil.isEmpty(idList)){
            return;
        }
        LambdaQueryWrapper<TcFormDynamicField> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(TcFormDynamicField::getFormInsId,idList);
        formDynamicFieldMapper.delete(lambdaQueryWrapper);
    }
}
