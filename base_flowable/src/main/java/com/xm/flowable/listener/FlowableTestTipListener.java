package com.xm.flowable.listener;

import cn.hutool.core.util.StrUtil;
import com.xm.core.enums.MsgActionTypeEnum;
import com.xm.core.msg.consts.MsgTypeConst;
import com.xm.core.msg.params.JumpUrlParam;
import com.xm.flowable.domain.params.CallBackOtherParams;
import com.xm.flowable.domain.params.FlowableMeta;
import com.xm.flowable.domain.res.FlowableMsgCreate;
import com.xm.flowable.domain.vo.ProcessInstanceVo;
import com.xm.flowable.domain.vo.TaskInfoVo;
import com.xm.flowable.enums.TaskStatusEnum;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FlowableTestTipListener implements FlowableTaskListener {
    @Override
    public void execute(ProcessInstanceVo processInstance, List<String> currentApproveUserIdList, TaskInfoVo taskInfoVo, TaskStatusEnum taskStatusEnum, boolean isFinish, CallBackOtherParams params) {

    }

    @Override
    public List<FlowableMsgCreate> getMsglist(ProcessInstanceVo processInstance, List<String> currentApproveUserIdList, TaskInfoVo taskInfoVo, TaskStatusEnum taskStatusEnum, boolean isFinish, CallBackOtherParams params) {
        JumpUrlParam jumpUrlParam=new JumpUrlParam();
        jumpUrlParam.setMsgActionTypeEnum(MsgActionTypeEnum.outDirectLink);
        jumpUrlParam.setOutDirectLink("https://www.baidu.com/");

        //待办消息
        FlowableMsgCreate taskTip=new FlowableMsgCreate();
        taskTip.setTitle("测试流程审批提醒");
        Map<String,Object> otherMap=new LinkedHashMap<>();
        otherMap.put("测试1","测试1");
        otherMap.put("测试2","测试2");
        otherMap.put("测试3","测试3");
        otherMap.put("测试4","测试4");
        otherMap.put("测试5","测试5");
        taskTip.setOtherMap(otherMap);
        taskTip.setJumpUrlParam(jumpUrlParam);
        taskTip.setMsgTypeList(Collections.singletonList(MsgTypeConst.ddTaskTip));
        taskTip.setUserIdList(currentApproveUserIdList);
        return Collections.singletonList(taskTip);
    }

    @Override
    public List<FlowableMsgCreate> getCcMsglist(DelegateExecution delegateExecution, String category, List<String> ccUserList) {
        JumpUrlParam jumpUrlParam=new JumpUrlParam();
        jumpUrlParam.setMsgActionTypeEnum(MsgActionTypeEnum.outDirectLink);
        jumpUrlParam.setOutDirectLink("https://www.baidu.com/");

        //机器人消息
        FlowableMsgCreate robotMsg=new FlowableMsgCreate();
        robotMsg.setTitle("测试抄送提醒");
        robotMsg.setContent(StrUtil.format("测试抄送提醒,category->{}",category));
        robotMsg.setJumpUrlParam(jumpUrlParam);
        robotMsg.setMsgTypeList(Collections.singletonList(MsgTypeConst.ddRobotMsg));
        robotMsg.setUserIdList(ccUserList);
        return Collections.singletonList(robotMsg);
    }

    @Override
    public List<FlowableMeta> getMeta() {
        return Collections.emptyList();
    }
}
