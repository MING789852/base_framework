package com.xm.form.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.form.domain.entity.TcFormLabel;
import com.xm.form.domain.entity.TcFormLabelRef;
import com.xm.form.mapper.TcFormLabelMapper;
import com.xm.form.mapper.TcFormLabelRefMapper;
import com.xm.form.service.TcFormLabelService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.valid.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TcFormLabelServiceImpl implements TcFormLabelService {

    private final TcFormLabelMapper labelMapper;
    private final TcFormLabelRefMapper labelRefMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(String moduleName, String refId, List<TcFormLabel> labelList) {
        if (moduleName==null){
            throw new CommonException("[标签]模块为空");
        }
        if (StrUtil.isBlank(refId)){
            throw new CommonException("[标签]业务id为空");
        }
        //先删除关联
        delete(moduleName, refId);

        //再新增关联
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，标签无法保存");
        }
        Date now=new Date();
        LambdaQueryWrapper<TcFormLabel> labelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(labelList)){
            for (TcFormLabel label:labelList){
                ValidationUtils.validateEntityIgnore(label, Collections.singletonList("id"));
                labelLambdaQueryWrapper.clear();
                labelLambdaQueryWrapper.eq(TcFormLabel::getLabelName,label.getLabelName());
                TcFormLabel database = labelMapper.selectOne(labelLambdaQueryWrapper);
                if (database==null){
                    label.setId(SnowIdUtil.getSnowId());
                    label.setCreateDate(now);
                    label.setCreateUser(currentLoginUserBySession.getNickName());
                    labelMapper.insert(label);
                }else {
                    label.setId(database.getId());
                }

                //新增关联
                TcFormLabelRef ref=new TcFormLabelRef();
                ref.setRefLabelId(label.getId());
                ref.setRefBusinessId(refId);
                ref.setRefType(moduleName);
                ref.setCreateDate(now);
                ref.setCreateUser(currentLoginUserBySession.getNickName());

                labelRefMapper.insert(ref);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String moduleName, String refId) {
        LambdaQueryWrapper<TcFormLabelRef> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcFormLabelRef::getRefBusinessId,refId).eq(TcFormLabelRef::getRefType,moduleName);
        labelRefMapper.delete(lambdaQueryWrapper);
    }

    @Override
    public List<TcFormLabel> getFormLabelList(String moduleName, String refId) {
        LambdaQueryWrapper<TcFormLabelRef> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcFormLabelRef::getRefBusinessId,refId).eq(TcFormLabelRef::getRefType,moduleName);

        List<TcFormLabelRef> tcPatentLabelRefs = labelRefMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtil.isNotEmpty(tcPatentLabelRefs)){
            List<String> labelIdList = tcPatentLabelRefs.stream().map(TcFormLabelRef::getRefLabelId).collect(Collectors.toList());
            LambdaQueryWrapper<TcFormLabel> labelLambdaQueryWrapper=new LambdaQueryWrapper<>();
            labelLambdaQueryWrapper.in(TcFormLabel::getId,labelIdList);
            return labelMapper.selectList(labelLambdaQueryWrapper);
        }else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<TcFormLabel> getAllFormLabelList() {
        LambdaQueryWrapper<TcFormLabel> labelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        return labelMapper.selectList(labelLambdaQueryWrapper);
    }

    @Override
    public Map<String, String> getAllLabelNameMapping() {
        List<TcFormLabel> allPatentLabelList = getAllFormLabelList();
        if (CollectionUtil.isEmpty(allPatentLabelList)){
            return new HashMap<>();
        }
        return allPatentLabelList.stream().collect(Collectors.toMap(TcFormLabel::getLabelName,TcFormLabel::getLabelName));
    }
}
