package com.xm.flowable.listener;

import com.xm.flowable.domain.params.CallBackOtherParams;
import com.xm.flowable.domain.params.FlowableMeta;
import com.xm.flowable.domain.res.FlowableMsgCreate;
import com.xm.flowable.domain.vo.ProcessInstanceVo;
import com.xm.flowable.domain.vo.TaskInfoVo;
import com.xm.flowable.enums.TaskStatusEnum;
import org.flowable.engine.delegate.DelegateExecution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public interface FlowableTaskListener extends Serializable {

    //待办执行
    void execute(ProcessInstanceVo processInstance,
                 List<String> currentApproveUserIdList,
                 TaskInfoVo taskInfoVo,
                 TaskStatusEnum taskStatusEnum,
                 boolean isFinish, CallBackOtherParams params);

    //发送消息
    List<FlowableMsgCreate> getMsglist(ProcessInstanceVo processInstance,
                                       List<String> currentApproveUserIdList,
                                       TaskInfoVo taskInfoVo,
                                       TaskStatusEnum taskStatusEnum,
                                       boolean isFinish, CallBackOtherParams params);

    //发送抄送消息
    default List<FlowableMsgCreate> getCcMsglist(DelegateExecution delegateExecution,
                                                 String category,
                                                 List<String> ccUserList) {
        return new ArrayList<>();
    }

    //获取流程元数据
    List<FlowableMeta> getMeta();

}
