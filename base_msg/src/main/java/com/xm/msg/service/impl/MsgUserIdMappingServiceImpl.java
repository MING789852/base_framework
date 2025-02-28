package com.xm.msg.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.msg.MsgUserIdMappingService;
import com.xm.msg.domain.entity.TcMsgUserMapping;
import com.xm.msg.mapper.TcMsgUserMappingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MsgUserIdMappingServiceImpl implements MsgUserIdMappingService {

    private final TcMsgUserMappingMapper msgUserMappingMapper;

    @Override
    public String getMapping(String type,String userId) {
        if (StrUtil.isBlank(type)){
            throw new CommonException("类型为空，无法获取用户消息映射");
        }
        if (StrUtil.isBlank(userId)){
            throw new CommonException("用户ID为空，无法获取用户消息映射");
        }
        LambdaQueryWrapper<TcMsgUserMapping> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TcMsgUserMapping::getUserId,userId)
                .eq(TcMsgUserMapping::getType,type);
        TcMsgUserMapping mapping = msgUserMappingMapper.selectOne(lambdaQueryWrapper);
        if (mapping==null){
            return null;
        }
        return mapping.getMapping();
    }

    @Override
    public void saveMapping(String type,String userId, String mapping) {
        if (StrUtil.isBlank(type)){
            throw new CommonException("类型为空，无法存储用户消息映射");
        }
        if (StrUtil.isBlank(userId)){
            throw new CommonException("用户ID为空，无法存储用户消息映射");
        }
        if (StrUtil.isBlank(mapping)){
            throw new CommonException("映射数据为空，无法存储用户消息映射");
        }
        TcMsgUserMapping msgUserMapping=new TcMsgUserMapping();
        msgUserMapping.setType(type);
        msgUserMapping.setUserId(userId);
        msgUserMapping.setMapping(mapping);
        msgUserMappingMapper.insert(msgUserMapping);
    }
}
