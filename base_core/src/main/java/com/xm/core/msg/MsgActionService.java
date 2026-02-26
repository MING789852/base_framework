package com.xm.core.msg;

public interface MsgActionService {
    //跳转完成后消息是否结束
    String jumpAfterFinishMsg(String businessType, String businessKey, boolean all);
}
