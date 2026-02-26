package com.xm.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.mapper.TcUserMapper;
import com.xm.core.msg.params.JumpUrlParam;
import com.xm.core.msg.params.Msg;
import com.xm.flowable.domain.entity.TcBusinessApproving;
import com.xm.flowable.domain.res.FlowableMsgCreate;
import com.xm.flowable.enums.ProcessVariableNameEnum;
import com.xm.flowable.enums.TaskStatusEnum;
import com.xm.flowable.mapper.TcBusinessApprovingMapper;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.msg.MsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FlowableMsgUtil {

    private static TcBusinessApprovingMapper approvingMapper;
    private static TcUserMapper tcUserMapper;
    private static TransactionTemplate transactionTemplate;

    @Autowired
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        FlowableMsgUtil.transactionTemplate = transactionTemplate;
    }


    @Autowired
    public  void setApprovingMapper(TcBusinessApprovingMapper approvingMapper) {
        FlowableMsgUtil.approvingMapper = approvingMapper;
    }

    @Autowired
    public  void setTcUserMapper(TcUserMapper tcUserMapper) {
        FlowableMsgUtil.tcUserMapper = tcUserMapper;
    }


    public static void handleApprovingMsg(String processInstanceId,
                                          Map<String, Object> processVariables,
                                          TaskStatusEnum taskStatusEnum,
                                          List<String> currentApproveUserIdList,
                                          List<FlowableMsgCreate> flowableMsgCreates,
                                          boolean isFinish,
                                          String businessId,
                                          String businessType){
        if (StrUtil.isBlank(processInstanceId)){
            throw new CommonException("流程为空，审批进度初始化失败");
        }
        TcUser userBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (userBySession==null){
            throw new CommonException("用户不存在，审批进度初始化失败");
        }
        String creatorId = FlowableUtil.getMapVariable(processVariables, ProcessVariableNameEnum.creatorId.name());
        LambdaQueryWrapper<TcBusinessApproving> approvingLambdaQueryWrapper=new LambdaQueryWrapper<>();

        if (TaskStatusEnum.delete.equals(taskStatusEnum)){
            approvingLambdaQueryWrapper.clear();
            approvingLambdaQueryWrapper
                    .eq(TcBusinessApproving::getBusinessType,businessType)
                    .eq(TcBusinessApproving::getBusinessId,businessId);
            approvingMapper.delete(approvingLambdaQueryWrapper);
            FlowableMsgUtil.executeSend(creatorId,businessType, businessId,null, FlowableConst.TASK_TIP_TYPE_ALL_DELETE,null);
            log.info("流程删除，删除全部消息");
        } else if (Arrays.asList(TaskStatusEnum.rollback,TaskStatusEnum.rollBackRecordMsg).contains(taskStatusEnum)&&isFinish){
            approvingLambdaQueryWrapper.clear();
            approvingLambdaQueryWrapper
                    .eq(TcBusinessApproving::getBusinessType,businessType)
                    .eq(TcBusinessApproving::getBusinessId,businessId);
            approvingMapper.delete(approvingLambdaQueryWrapper);
            FlowableMsgUtil.executeSend(creatorId,businessType, businessId,null,FlowableConst.TASK_TIP_TYPE_ALL_DELETE,null);
            log.info("流程回滚到开始节点，删除全部消息");
        } else {
            //获取之前审批任务人
            approvingLambdaQueryWrapper
                    .eq(TcBusinessApproving::getBusinessType,businessType)
                    .eq(TcBusinessApproving::getBusinessId,businessId);
            List<TcBusinessApproving> tcBusinessApprovings = approvingMapper.selectList(approvingLambdaQueryWrapper);
            List<String> preApproveUserIdList = tcBusinessApprovings.stream().map(TcBusinessApproving::getUserId).collect(Collectors.toList());


            //新增正在审批人员
            List<String> copyCurrentApproveUserIdList = BeanUtil.copyToList(currentApproveUserIdList, String.class);
            copyCurrentApproveUserIdList.removeAll(preApproveUserIdList);
            if (CollectionUtil.isNotEmpty(copyCurrentApproveUserIdList)){
                for (String userId:copyCurrentApproveUserIdList){
                    TcBusinessApproving approving=new TcBusinessApproving();
                    approving.setBusinessType(businessType);
                    approving.setBusinessId(businessId);
                    approving.setId(SnowIdUtil.getSnowId());
                    approving.setUserId(userId);
                    TcUser user = tcUserMapper.selectById(approving.getUserId());
                    if (user==null){
                        throw new CommonException(StrUtil.format("下一流程审批人【{}】系统不存在,审批失败",userId));
                    }
                    approving.setUserName(user.getNickName());
                    approvingMapper.insert(approving);
                }
            }

            //删除历史正在审批人员
            List<String> copyPreApproveUserIdList = BeanUtil.copyToList(preApproveUserIdList, String.class);
            copyPreApproveUserIdList.removeAll(currentApproveUserIdList);
            if (CollectionUtil.isNotEmpty(copyPreApproveUserIdList)){
                approvingLambdaQueryWrapper.clear();
                approvingLambdaQueryWrapper
                        .eq(TcBusinessApproving::getBusinessType,businessType)
                        .eq(TcBusinessApproving::getBusinessId,businessId)
                        .in(TcBusinessApproving::getUserId,copyPreApproveUserIdList);
                approvingMapper.delete(approvingLambdaQueryWrapper);

                if (Arrays.asList(TaskStatusEnum.rollback,TaskStatusEnum.rollBackRecordMsg).contains(taskStatusEnum)
                        ||TaskStatusEnum.transfer.equals(taskStatusEnum)){
                    FlowableMsgUtil.executeSend(creatorId,businessType, businessId, preApproveUserIdList, FlowableConst.TASK_TIP_TYPE_DELETE,null);
                }else {
                    FlowableMsgUtil.executeSend(creatorId,businessType, businessId, copyPreApproveUserIdList, FlowableConst.TASK_TIP_TYPE_FINISH,null);
                }
            }

            //发送消息
            if (CollectionUtil.isNotEmpty(flowableMsgCreates)){
                for (FlowableMsgCreate flowableMsgResult:flowableMsgCreates){
                    FlowableMsgUtil.executeSend(creatorId,businessType,businessId,null,FlowableConst.TASK_TIP_TYPE_UN_FINISH,flowableMsgResult);
                }
            }
        }
    }

    /*
    用于流程定义修改审批人员消息转发
     */
    public static void handleChangeAssigneeMsg(
            String creatorId, String businessType,
            String businessKey,
            List<String> oldAssigneeList, List<FlowableMsgCreate> newFlowableMsgCreates) {

        //删除旧消息
        FlowableMsgUtil.executeSend(creatorId, businessType, businessKey, oldAssigneeList, FlowableConst.TASK_TIP_TYPE_DELETE, null);
        //发送消息
        if (CollectionUtil.isNotEmpty(newFlowableMsgCreates)) {
            for (FlowableMsgCreate flowableMsgResult : newFlowableMsgCreates) {
                FlowableMsgUtil.executeSend(creatorId, businessType, businessKey, null, FlowableConst.TASK_TIP_TYPE_UN_FINISH, flowableMsgResult);
            }
        }
    }


    /**
     * 发送审批流消息
     * @param flowableMsgCreate 创建消息
     * @param businessType 业务类型
     * @param businessKey 业务KEY
     * @param taskTipType FlowableConst常量定义
     */
     public static void executeSend(String creatorId,String businessType,
                            String businessKey,
                            List<String> deleteOrFinishUserIdList,
                            String taskTipType, FlowableMsgCreate flowableMsgCreate){
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            try {
                if (FlowableConst.TASK_TIP_TYPE_ALL_DELETE.equals(taskTipType)){
                    MsgUtil.deleteMsgList(businessType,businessKey);
                }else{
                    if (FlowableConst.TASK_TIP_TYPE_DELETE.equals(taskTipType)){
                        for (String userId:deleteOrFinishUserIdList) {
                            MsgUtil.deleteMsg(businessType,businessKey,userId);
                        }
                    }
                    if (FlowableConst.TASK_TIP_TYPE_FINISH.equals(taskTipType)){
                        for (String userId:deleteOrFinishUserIdList) {
                            MsgUtil.finishMsg(businessType,businessKey,userId);
                        }
                    }
                    if (FlowableConst.TASK_TIP_TYPE_UN_FINISH.equals(taskTipType)){
                        List<String> userIdList = flowableMsgCreate.getUserIdList();
                        List<String> msgTypeList = flowableMsgCreate.getMsgTypeList();
                        JumpUrlParam jumpUrlParam = flowableMsgCreate.getJumpUrlParam();
                        Map<String, Object> otherMap = flowableMsgCreate.getOtherMap();
                        String title = flowableMsgCreate.getTitle();
                        String content = flowableMsgCreate.getContent();
                        if (CollectionUtil.isEmpty(userIdList)){
                            return;
                        }
                        List<Msg> msgList=new ArrayList<>();
                        for (String userId:userIdList) {
                            Msg msg = Msg.builder()
                                    .title(title)
                                    .content(content)
                                    .businessType(businessType)
                                    .businessKey(businessKey)
                                    .jumpUrlParam(jumpUrlParam)
                                    .creatorId(creatorId)
                                    .otherMap(otherMap)
                                    .toUserIdList(Collections.singletonList(userId))
                                    .judgeFinish(false).build();
                            msgList.add(msg);
                        }
                        if (CollectionUtil.isNotEmpty(msgTypeList)){
                            MsgUtil.createOrUpdateMsg(msgList, msgTypeList);
                        }
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
