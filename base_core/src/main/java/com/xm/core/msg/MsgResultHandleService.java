package com.xm.core.msg;

import com.xm.core.msg.params.MsgSendResult;

import java.util.List;

public interface MsgResultHandleService {
    void execute(List<MsgSendResult> resultList);
}
