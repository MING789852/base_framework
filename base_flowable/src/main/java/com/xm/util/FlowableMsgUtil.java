package com.xm.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.msg.consts.MsgTypeConst;
import com.xm.core.msg.params.JumpUrlParam;
import com.xm.core.msg.params.Msg;
import com.xm.util.msg.MsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class FlowableMsgUtil {

    private static TransactionTemplate transactionTemplate;

    @Autowired
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        FlowableMsgUtil.transactionTemplate = transactionTemplate;
    }

    /**
     * 发送审批流消息
     * @param title 标题
     * @param content 消息内容
     * @param businessType 业务类型
     * @param businessKey 业务KEY
     * @param userIdList 接收人ID
     * @param taskTipType FlowableConst常量定义
     * @param msgTypeList MsgTypeConst常量定义
     * @param jumpUrlParam 跳转url
     */
    static void executeSend(String title,
                            String content,
                            String businessType,
                            String businessKey,
                            List<String> userIdList,
                            String taskTipType,
                            List<String> msgTypeList,
                            JumpUrlParam jumpUrlParam){
        if (StrUtil.isBlank(content)){
            return;
        }
        if (CollectionUtil.isEmpty(msgTypeList)){
            return;
        }
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            try {
                if (FlowableConst.TASK_TIP_TYPE_ALL_DELETE.equals(taskTipType)){
                    MsgUtil.deleteMsgList(businessType,businessKey);
                }else{
                    if (CollectionUtil.isEmpty(userIdList)){
                        return;
                    }
                    if (FlowableConst.TASK_TIP_TYPE_DELETE.equals(taskTipType)){
                        for (String userId:userIdList) {
                            MsgUtil.deleteMsg(businessType,businessKey,userId);
                        }
                    }
                    if (FlowableConst.TASK_TIP_TYPE_FINISH.equals(taskTipType)){
                        for (String userId:userIdList) {
                            MsgUtil.finishMsg(businessType,businessKey,userId);
                        }
                    }
                    if (FlowableConst.TASK_TIP_TYPE_UN_FINISH.equals(taskTipType)){
                        List<Msg> msgList=new ArrayList<>();
                        for (String userId:userIdList) {
                            Msg msg = Msg.builder()
                                    .title(title)
                                    .content(content)
                                    .businessType(businessType)
                                    .businessKey(businessKey)
                                    .jumpUrlParam(jumpUrlParam)
                                    .toUserIdList(Collections.singletonList(userId))
                                    .judgeFinish(false).build();
                            msgList.add(msg);
                        }
                        MsgUtil.createOrUpdateMsg(msgList, msgTypeList);
                    }
                }
            }catch (Exception e){
                transactionStatus.setRollbackOnly();
                log.error("执行审批流发送失败,开始回滚事务",e);
                //抛出异常使事务回滚
                throw new CommonException(e.getMessage());
            }
        });
    }

}
