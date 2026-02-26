package com.xm.msg.service.impl;

import com.xm.auth.domain.entity.TcUser;
import com.xm.core.msg.MsgActionService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.msg.MsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MsgActionServiceImpl implements MsgActionService {
    @Override
    public String jumpAfterFinishMsg(String businessType, String businessKey, boolean all) {
        if (all){
            MsgUtil.finishMsgList(businessType,businessKey);
        }else {
            TcUser tcUser = UserInfoUtil.getCurrentLoginUserBySessionOrTokenNotNull();
            MsgUtil.finishMsg(businessType,businessKey,tcUser.getId());
        }
        return "操作成功";
    }
}
