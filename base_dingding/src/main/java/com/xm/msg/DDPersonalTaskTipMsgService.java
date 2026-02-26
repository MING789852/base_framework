package com.xm.msg;

import com.xm.core.msg.MsgSaveService;
import com.xm.core.msg.consts.MsgTypeConst;
import com.xm.core.msg.params.Msg;
import com.xm.core.msg.params.MsgSendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service(MsgTypeConst.ddPersonalTaskTip)
public class DDPersonalTaskTipMsgService extends DDTaskTipMsgService {

    public DDPersonalTaskTipMsgService(MsgSaveService msgSaveService, DDUnionIdMappingService mappingService) {
        super(msgSaveService, mappingService);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MsgSendResult createOrUpdateMsg(List<Msg> msgList) {
        for (Msg msg : msgList){
            msg.setJumpUrlParam(null);
        }
        return super.createOrUpdateMsg(msgList);
    }

    @Override
    public String getMsgType() {
        return MsgTypeConst.ddPersonalTaskTip;
    }
}
