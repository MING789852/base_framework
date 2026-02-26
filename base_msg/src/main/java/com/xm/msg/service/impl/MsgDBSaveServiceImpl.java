package com.xm.msg.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.msg.MsgSaveService;
import com.xm.core.msg.params.Msg;
import com.xm.msg.domain.entity.TcMsg;
import com.xm.msg.mapper.TcMsgMapper;
import com.xm.msg.util.MsgConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MsgDBSaveServiceImpl implements MsgSaveService {

    private final TcMsgMapper msgMapper;


    @Override
    public Msg getMsg(String businessType, String businessKey,String msgType, String userId) {
        if (StrUtil.isBlank(businessKey)){
            throw new CommonException("业务key为空，无法获取数据");
        }
        if (StrUtil.isBlank(businessType)){
            throw new CommonException("业务类型为空，无法获取数据");
        }
        if (StrUtil.isBlank(userId)){
            throw new CommonException("用户ID为空，无法获取数据");
        }
        LambdaQueryWrapper<TcMsg> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TcMsg::getBusinessType,businessType)
                .eq(TcMsg::getBusinessKey,businessKey)
                .eq(TcMsg::getType,msgType)
                .eq(TcMsg::getUserId,userId);
        TcMsg msg = msgMapper.selectOne(lambdaQueryWrapper);
        return MsgConvertUtil.convert(msg);
    }

    @Override
    public List<Msg> getMsgList(String businessType, String businessKey,String msgType) {
        if (StrUtil.isBlank(businessKey)){
            throw new CommonException("业务key为空，无法获取数据");
        }
        if (StrUtil.isBlank(businessType)){
            throw new CommonException("业务类型为空，无法获取数据");
        }
        LambdaQueryWrapper<TcMsg> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TcMsg::getBusinessType,businessType)
                .eq(TcMsg::getBusinessKey,businessKey)
                .eq(TcMsg::getType,msgType);
        List<TcMsg> tcMsgList = msgMapper.selectList(lambdaQueryWrapper);

        if (CollectionUtil.isEmpty(tcMsgList)){
            return new ArrayList<>();
        }else {
            return tcMsgList.stream().map(MsgConvertUtil::convert).collect(Collectors.toList());
        }
    }

    @Override
    public boolean saveMsg(Msg msg) {
        if (msg==null){
            throw new CommonException("消息为空，无法保存");
        }
        List<TcMsg> tcMsgList = MsgConvertUtil.convert(msg);
        Date now=new Date();
        for (TcMsg tcMsg : tcMsgList){
            LambdaQueryWrapper<TcMsg> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper
                    .eq(TcMsg::getBusinessType,tcMsg.getBusinessType())
                    .eq(TcMsg::getBusinessKey,tcMsg.getBusinessKey())
                    .eq(TcMsg::getType,tcMsg.getType())
                    .eq(TcMsg::getUserId,tcMsg.getUserId());
            TcMsg database = msgMapper.selectOne(lambdaQueryWrapper);
            if (database==null){
                tcMsg.setCreateDate(now);
                msgMapper.insert(tcMsg);
            }else {
                tcMsg.setId(database.getId());
                tcMsg.setUpdateDate(now);
                msgMapper.updateById(tcMsg);
            }
        }
        return true;
    }

    @Override
    public boolean deleteMsg(String id) {
        return msgMapper.deleteById(id)>0;
    }
}
