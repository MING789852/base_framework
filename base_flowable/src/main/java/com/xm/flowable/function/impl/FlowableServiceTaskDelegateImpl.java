package com.xm.flowable.function.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.msg.params.Msg;
import com.xm.flowable.domain.res.FlowableMsgCreate;
import com.xm.flowable.enums.ProcessVariableNameEnum;
import com.xm.flowable.enums.ServiceTaskActionTypeEnum;
import com.xm.flowable.enums.ServiceTaskVariableNameEnum;
import com.xm.flowable.listener.FlowableTaskListener;
import com.xm.flowable.function.FlowableBeanFunction;
import com.xm.util.FlowableUtil;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.msg.MsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service("serviceTaskFunction")
public class FlowableServiceTaskDelegateImpl implements FlowableBeanFunction {


    public void execute(DelegateExecution delegateExecution,String type,String category) {
        try {
            if (ServiceTaskActionTypeEnum.cc.name().equals(type)){
                FlowElement currentFlowElement = delegateExecution.getCurrentFlowElement();
                Map<String, String> mapDataFromFlowableElement = FlowableUtil.getMapDataFromFlowableElement(currentFlowElement);
                List<String> ccUserList=Optional.ofNullable(mapDataFromFlowableElement.get(ServiceTaskVariableNameEnum.ccUserList.name()))
                        .map(item->JSONUtil.toList(item, String.class))
                        .orElse(new ArrayList<>());
                String listenerClassName = delegateExecution.getVariable(ProcessVariableNameEnum.listener.name(),String.class);
                String businessType = delegateExecution.getVariable(ProcessVariableNameEnum.businessType.name(),String.class);
                String businessKey = delegateExecution.getVariable(ProcessVariableNameEnum.businessKey.name(),String.class);

                FlowableTaskListener beanByClass = (FlowableTaskListener) SpringBeanUtil.getBeanByClass(Class.forName(listenerClassName));
                List<FlowableMsgCreate> ccMsglist = beanByClass.getCcMsglist(delegateExecution,category, ccUserList);
                if (CollectionUtil.isNotEmpty(ccMsglist)){
                    for (FlowableMsgCreate flowableMsgCreate : ccMsglist){
                        Msg msg= Msg.builder()
                                .jumpAfterFinish(true)
                                .title(flowableMsgCreate.getTitle())
                                .businessKey(businessKey)
                                .businessType(businessType)
                                .content(flowableMsgCreate.getContent())
                                .jumpUrlParam(flowableMsgCreate.getJumpUrlParam())
                                .toUserIdList(flowableMsgCreate.getUserIdList())
                                .otherMap(flowableMsgCreate.getOtherMap())
                                .build();
                        MsgUtil.createOrUpdateMsg(Collections.singletonList(msg),flowableMsgCreate.getMsgTypeList());
                    }
                }
            }
        }catch (Exception e){
            log.error("【ServiceTask】服务执行失败",e);
            throw new CommonException("【ServiceTask】服务执行失败->{}"+e.getMessage());
        }
    }
}
