package com.xm.status.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.status.domain.entity.TcStatusMainInstance;
import com.xm.status.mapper.TcStatusMainInstanceMapper;
import com.xm.status.service.TcStatusMainInstanceService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcStatusMainInstanceServiceImpl implements TcStatusMainInstanceService {

    private final TcStatusMainInstanceMapper instanceMapper;

    @Override
    public TcStatusMainInstanceMapper getMapper() {
        return instanceMapper;
    }

    @Override
    public boolean existInstanceByMainModelId(String mainModelId) {
        LambdaQueryWrapper<TcStatusMainInstance> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcStatusMainInstance::getStatusMainModelId,mainModelId);
        Long count = instanceMapper.selectCount(lambdaQueryWrapper);
        return count.intValue() != 0;
    }

    @Override
    public TcStatusMainInstance getInstanceById(String id) {
        return instanceMapper.selectById(id);
    }

    @Override
    public TcStatusMainInstance createInstance(String statusInsCode,String statusInsName,String mainModelId,String refId,String refType) {
        if (StrUtil.isBlank(statusInsCode)){
            throw new CommonException("状态实例名称为空,无法创建表单实例");
        }
        if (StrUtil.isBlank(statusInsName)){
            throw new CommonException("状态实例编码为空,无法创建表单实例");
        }
        if (StrUtil.isBlank(refId)){
            throw new CommonException("关联ID为空,无法创建表单实例");
        }
        if (StrUtil.isBlank(refType)){
            throw new CommonException("关联类型为空,无法创建表单实例");
        }
        if (StrUtil.isBlank(mainModelId)){
            throw new CommonException("状态模型ID为空,无法创建表单实例");
        }
        Date now=new Date();
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录,无权限操作");
        }
        TcStatusMainInstance instance=new TcStatusMainInstance();
        instance.setId(SnowIdUtil.getSnowId());
        instance.setCode(statusInsCode);
        instance.setName(statusInsName);
        instance.setStatusMainModelId(mainModelId);
        instance.setRefId(refId);
        instance.setRefType(refType);
        instance.setFinish(false);
        instance.setCreateDate(now);
        instance.setCreateUser(currentLoginUserBySession.getNickName());
        instanceMapper.insert(instance);
        return instance;
    }
}
