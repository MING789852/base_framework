package com.xm.core.msg;

import com.xm.core.msg.params.Msg;

import java.util.List;

public interface MsgSaveService {

    Msg getMsg(String businessType, String businessKey,String msgType, String userId);

    List<Msg> getMsgList(String businessType, String businessKey,String msgType);

    boolean saveMsg(Msg msg);

    boolean deleteMsg(String id);
}
