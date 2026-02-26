package com.xm.msg.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.msg.params.Msg;
import com.xm.msg.domain.entity.TcMsg;
import com.xm.util.id.SnowIdUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MsgConvertUtil {
    public static Msg convert(TcMsg tcMsg){
        if (tcMsg==null){
            return null;
        }
        Msg msg = Msg.builder().build();
        msg.setId(tcMsg.getId());
        msg.setType(tcMsg.getType());
        msg.setBusinessType(tcMsg.getBusinessType());
        msg.setBusinessKey(tcMsg.getBusinessKey());
        msg.setTitle(tcMsg.getTitle());
        msg.setContent(tcMsg.getContent());
        msg.setInfo(tcMsg.getInfo());
        msg.setJudgeFinish(tcMsg.getJudgeFinish());
        msg.setToUserIdList(Collections.singletonList(tcMsg.getUserId()));
        String groupAtAllStr = tcMsg.getGroupAtAll();
        Boolean groupAtAll;
        if (StrUtil.isBlank(groupAtAllStr)){
            groupAtAll=null;
        }else {
            groupAtAll= "1".equals(groupAtAllStr);
        }
        msg.setGroupAtAll(groupAtAll);
        msg.setGroupId(tcMsg.getGroupId());
        return msg;
    }

    public static List<TcMsg> convert(Msg msg){
        if (msg==null){
            return null;
        }
        List<String> userIdList = msg.getToUserIdList();
        if (CollectionUtil.isEmpty(userIdList)){
            throw new CommonException("接收消息人员为空,无法保存消息");
        }
        if (StrUtil.isBlank(msg.getType())){
            throw new CommonException("消息类型为空,无法保存消息");
        }
        if (StrUtil.isBlank(msg.getBusinessType())){
            throw new CommonException("业务类型为空，无法保存消息");
        }
        if (StrUtil.isBlank(msg.getBusinessKey())){
            throw new CommonException("业务主键为空，无法保存消息");
        }
        List<TcMsg> msgList=new ArrayList<>();
        for (String userId : userIdList){
            TcMsg tcMsg=new TcMsg();
            if (StrUtil.isBlank(msg.getId())){
                tcMsg.setId(SnowIdUtil.getSnowId());
            }else {
                tcMsg.setId(msg.getId());
            }
            tcMsg.setType(msg.getType());
            tcMsg.setBusinessType(msg.getBusinessType());
            tcMsg.setBusinessKey(msg.getBusinessKey());
            tcMsg.setTitle(msg.getTitle());
            tcMsg.setContent(msg.getContent());
            tcMsg.setInfo(msg.getInfo());
            tcMsg.setJudgeFinish(msg.isJudgeFinish());
            tcMsg.setUserId(userId);
            Boolean groupAtAll = msg.getGroupAtAll();
            if (groupAtAll!=null){
                tcMsg.setGroupAtAll(groupAtAll?"1":"0");
            }
            tcMsg.setGroupId(msg.getGroupId());

            msgList.add(tcMsg);
        }
        return msgList;
    }
}
