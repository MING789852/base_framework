package com.xm.core.msg;


import com.xm.core.msg.params.Msg;

import java.util.List;

public interface MsgService {

    boolean createOrUpdateMsg(List<Msg> msgList);

    boolean deleteMsgList(String businessType, String businessKey);

    boolean deleteMsg(String businessType, String businessKey,String userId);

    boolean finishMsgList(String businessType, String businessKey);

    boolean finishMsg(String businessType, String businessKey,String userId);

    String getMsgType();
}
