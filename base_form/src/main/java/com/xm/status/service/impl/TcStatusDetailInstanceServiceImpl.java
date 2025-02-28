package com.xm.status.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.status.domain.dto.TcStatusDetailInstanceDto;
import com.xm.status.domain.entity.TcStatusDetailInstance;
import com.xm.status.mapper.TcStatusDetailInstanceMapper;
import com.xm.status.service.TcStatusDetailInstanceService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcStatusDetailInstanceServiceImpl implements TcStatusDetailInstanceService {

    private final TcStatusDetailInstanceMapper detailInstanceMapper;

    @Override
    public TcStatusDetailInstanceMapper getMapper() {
        return detailInstanceMapper;
    }

    @Override
    public String saveOrUpdateData(List<TcStatusDetailInstance> list) {
        Date now=new Date();
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，无权限操作");
        }
        if (CollectionUtil.isNotEmpty(list)){
            for (TcStatusDetailInstance instance:list){
                if (StrUtil.isBlank(instance.getId())){
                    instance.setId(SnowIdUtil.getSnowId());
                    instance.setCreateDate(now);
                    instance.setCreateUser(currentLoginUserBySession.getNickName());
                    detailInstanceMapper.insert(instance);
                }else {
                    instance.setUpdateDate(now);
                    instance.setUpdateUser(currentLoginUserBySession.getNickName());
                    detailInstanceMapper.updateById(instance);
                }
            }
        }
        return "操作成功";
    }

    @Override
    public void saveDtoData(String statusMainInstanceId, List<TcStatusDetailInstanceDto> detailInstanceDtoList) {
        //先删除后保存
        LambdaQueryWrapper<TcStatusDetailInstance> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcStatusDetailInstance::getStatusMainInstanceId,statusMainInstanceId);
        detailInstanceMapper.delete(lambdaQueryWrapper);

        //保存
        List<TcStatusDetailInstance> tcStatusDetailInstances = BeanUtil.copyToList(detailInstanceDtoList, TcStatusDetailInstance.class);
        detailInstanceMapper.insert(tcStatusDetailInstances);
    }

    @Override
    public boolean existsStatusDetailInstance(String statusMainInstanceId, String statusDetailModelId) {
        LambdaQueryWrapper<TcStatusDetailInstance> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcStatusDetailInstance::getStatusMainInstanceId,statusMainInstanceId);
        lambdaQueryWrapper.eq(TcStatusDetailInstance::getStatusDetailModelId,statusDetailModelId);
        Long count = detailInstanceMapper.selectCount(lambdaQueryWrapper);
        return count.intValue()!=0;
    }

    @Override
    public List<TcStatusDetailInstance> getStatusDetailInstanceByMainInsId(String mainInsId) {
        LambdaQueryWrapper<TcStatusDetailInstance> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcStatusDetailInstance::getStatusMainInstanceId,mainInsId);
        return detailInstanceMapper.selectList(lambdaQueryWrapper);
    }
}
