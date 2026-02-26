package com.xm.msg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.core.enums.MsgActionTypeEnum;
import com.xm.core.msg.consts.MsgTypeConst;
import com.xm.core.msg.consts.MsgTypeItem;
import com.xm.core.msg.params.JumpUrlParam;
import com.xm.core.msg.params.Msg;
import com.xm.module.core.params.QueryData;
import com.xm.msg.domain.dto.MsgDto;
import com.xm.msg.domain.entity.TcMsg;
import com.xm.msg.mapper.TcMsgMapper;
import com.xm.msg.service.MsgMainModelService;
import com.xm.util.msg.MsgUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MsgMainModelServiceImpl implements MsgMainModelService {

    private final TcMsgMapper tcMsgMapper;

    @Override
    public TcMsgMapper getMapper() {
        return tcMsgMapper;
    }

    @Override
    public Page<TcMsg> selectByPage(QueryData<TcMsg> queryData) {
        queryData.getWrapper().orderByDesc("create_date");
        return MsgMainModelService.super.selectByPage(queryData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteData(List<TcMsg> list) {
        if (CollectionUtil.isNotEmpty(list)){
            for (TcMsg tcMsg : list){
                String businessKey = tcMsg.getBusinessKey();
                String businessType = tcMsg.getBusinessType();
                String userId = tcMsg.getUserId();
                if (StrUtil.isNotBlank(businessKey)&&StrUtil.isNotBlank(businessType)&&StrUtil.isNotBlank(userId)){
                    MsgUtil.deleteMsg(businessType,businessKey,userId);
                }
            }
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateMsg(List<MsgDto> list) {
        if (CollectionUtil.isNotEmpty(list)){
            //清空额外消息
            List<Msg> msgList=new ArrayList<>();
            for (MsgDto msgDto : list){
                Msg msg= BeanUtil.copyProperties(msgDto,Msg.class);
                msg.setInfo("");
                String jumpUrl = msgDto.getJumpUrl();
                if (StrUtil.isNotBlank(jumpUrl)){
                    JumpUrlParam jumpUrlParam=new JumpUrlParam();
                    jumpUrlParam.setMsgActionTypeEnum(MsgActionTypeEnum.outDirectLink);
                    jumpUrlParam.setOutDirectLink(jumpUrl);
                    msg.setJumpUrlParam(jumpUrlParam);
                }
                msgList.add(msg);
            }
            MsgUtil.createOrUpdateMsg(msgList);
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String finishMsg(List<Msg> list) {
        if (CollectionUtil.isNotEmpty(list)){
            for (Msg msg : list){
                TcMsg tcMsg = tcMsgMapper.selectById(msg.getId());
                if (tcMsg==null){
                    continue;
                }
                MsgUtil.finishMsgByMsgType(tcMsg.getType(),tcMsg.getBusinessType(),tcMsg.getBusinessKey(),tcMsg.getUserId());
            }
        }
        return "操作成功";
    }

    @Override
    public Map<String, Object> getDictMapping() {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> msgTypeItemMap = MsgTypeConst.msgTypeItemList.stream().collect(Collectors.toMap(MsgTypeItem::getCode, MsgTypeItem::getLabel));
        result.put("type", msgTypeItemMap);
        Map<Boolean,String> judgeFinishDict=new HashMap<>();
        judgeFinishDict.put(true,"是");
        judgeFinishDict.put(false,"否");
        result.put("judgeFinish",judgeFinishDict);
        return result;
    }
}
