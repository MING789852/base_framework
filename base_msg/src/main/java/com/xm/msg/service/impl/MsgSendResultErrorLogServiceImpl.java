package com.xm.msg.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.xm.core.msg.MsgResultHandleService;
import com.xm.core.msg.params.ErrorSendResult;
import com.xm.core.msg.params.Msg;
import com.xm.core.msg.params.MsgSendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MsgSendResultErrorLogServiceImpl implements MsgResultHandleService {
    @Override
    public void execute(List<MsgSendResult> resultList) {
        if (CollectionUtil.isEmpty(resultList)){
            return;
        }
        for (MsgSendResult msgSendResult:resultList){
            List<Msg> failSendMsgList = msgSendResult.getFailSendMsgList();
            if (CollectionUtil.isEmpty(failSendMsgList)){
                continue;
            }
            for (Msg error:failSendMsgList){
                ErrorSendResult errorSendResult = error.getErrorSendResult();
                if (errorSendResult==null){
                    continue;
                }
                String errorMessage = errorSendResult.getMessage();
                String businessType = error.getBusinessType();
                String businessKey = error.getBusinessKey();
                String type = error.getType();
                log.error("【消息发送失败】消息类型->{},businessType->{},businessKey->{},errorMsg->{}",
                        type,businessType,businessKey,errorMessage);
            }
        }
    }
}
