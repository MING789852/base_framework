package com.xm.form.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.form.domain.entity.TcFormMainInstance;
import com.xm.form.mapper.TcFormMainInstanceMapper;
import com.xm.form.service.TcFormDynamicFieldService;
import com.xm.form.service.TcFormMainInstanceService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcFormMainInstanceServiceImpl implements TcFormMainInstanceService {

    private final TcFormMainInstanceMapper instanceMapper;

    private final TcFormDynamicFieldService formDynamicFieldService;

    @Override
    public TcFormMainInstanceMapper getMapper() {
        return instanceMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteData(List<TcFormMainInstance> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空");
        }
        //删除表单实例
        List<String> idList = list.stream().map(TcFormMainInstance::getId).collect(Collectors.toList());
        deleteDataById(idList);
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateData(List<TcFormMainInstance> list) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TcFormMainInstance createInstance(String formInsCode, String formInsName, String mainModelId, String refId, String refType) {
        if (StrUtil.isBlank(formInsName)){
            throw new CommonException("表单实例名称为空,无法创建表单实例");
        }
        if (StrUtil.isBlank(formInsCode)){
            throw new CommonException("表单实例编码为空,无法创建表单实例");
        }
        if (StrUtil.isBlank(refId)){
            throw new CommonException("关联ID为空,无法创建表单实例");
        }
        if (StrUtil.isBlank(refType)){
            throw new CommonException("关联类型为空,无法创建表单实例");
        }
        if (StrUtil.isBlank(mainModelId)){
            throw new CommonException("表单模型ID为空,无法创建表单实例");
        }
        Date now=new Date();
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录,无权限操作");
        }
        TcFormMainInstance instance=new TcFormMainInstance();
        instance.setId(SnowIdUtil.getSnowId());
        instance.setCode(formInsCode);
        instance.setName(formInsName);
        instance.setFormMainModelId(mainModelId);
        instance.setRefId(refId);
        instance.setRefType(refType);
        instance.setFinish(false);
        instance.setCreateDate(now);
        instance.setCreateUser(currentLoginUserBySession.getNickName());
        instanceMapper.insert(instance);
        return instance;
    }

    @Override
    public TcFormMainInstance getInstanceById(String id) {
        return instanceMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataById(List<String> idList) {
        if (CollectionUtil.isEmpty(idList)){
            return;
        }
        LambdaQueryWrapper<TcFormMainInstance> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(TcFormMainInstance::getId,idList);
        instanceMapper.delete(lambdaQueryWrapper);
        //删除动态字段实例
        formDynamicFieldService.deleteByFormInsIdList(idList);
    }

    @Override
    public boolean existById(String id) {
        LambdaQueryWrapper<TcFormMainInstance> instanceLambdaQueryWrapper=new LambdaQueryWrapper<>();
        instanceLambdaQueryWrapper.eq(TcFormMainInstance::getFormMainModelId,id);
        Long count = instanceMapper.selectCount(instanceLambdaQueryWrapper);
        return count.intValue() != 0;
    }
}
