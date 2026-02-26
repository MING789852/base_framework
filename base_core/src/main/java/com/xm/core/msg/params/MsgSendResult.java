package com.xm.core.msg.params;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MsgSendResult {
    private final List<Msg> successSendMsgList=new ArrayList<>();
    private final List<Msg> failSendMsgList=new ArrayList<>();

    public void addSuccessSendMsg(Msg msg){
        successSendMsgList.add(msg);
    }

    public void addSuccessSendMsgList(List<Msg> msgList){
        successSendMsgList.addAll(msgList);
    }

    public void addFailSendMsg(Msg msg,ErrorSendResult errorSendResult){
        msg.setErrorSendResult(errorSendResult);
        failSendMsgList.add(msg);
    }

    public void addFailSendMsgList(List<Msg> msgList,ErrorSendResult errorSendResult){
        for (Msg msg:msgList){
            msg.setErrorSendResult(errorSendResult);
        }
        failSendMsgList.addAll(msgList);
    }
}
