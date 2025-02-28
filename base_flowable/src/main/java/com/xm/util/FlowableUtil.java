package com.xm.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.mapper.TcUserMapper;
import com.xm.flowable.domain.entity.TcBusinessApproving;
import com.xm.flowable.domain.query.ActInst;
import com.xm.flowable.domain.vo.ProcessInstanceVo;
import com.xm.flowable.domain.vo.TaskInfoVo;
import com.xm.flowable.enums.ProcessVariableNameEnum;
import com.xm.flowable.enums.TaskStatusEnum;
import com.xm.flowable.enums.TaskVariableNameEnum;
import com.xm.flowable.mapper.TcBusinessApprovingMapper;
import com.xm.flowable.service.FlowableService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.core.msg.params.JumpUrlParam;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.impl.persistence.entity.HistoricProcessInstanceEntityImpl;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FlowableUtil {
    private static FlowableService flowableService;
    private static TcBusinessApprovingMapper approvingMapper;

    private static TcUserMapper tcUserMapper;

    @Autowired
    public  void setFlowableService(FlowableService flowableService) {
        FlowableUtil.flowableService = flowableService;
    }

    @Autowired
    public  void setApprovingMapper(TcBusinessApprovingMapper approvingMapper) {
        FlowableUtil.approvingMapper = approvingMapper;
    }

    @Autowired
    public  void setTcUserMapper(TcUserMapper tcUserMapper) {
        FlowableUtil.tcUserMapper = tcUserMapper;
    }

    public static String generaId(){
        return "fs_"+ SnowIdUtil.getSnowId();
    }

    public static void initApprovingData(String processInstanceId,
                                         TaskStatusEnum taskStatusEnum,
                                         String title,
                                         String content,
                                         List<String> msgTypeList,
                                         JumpUrlParam jumpUrlParam,
                                         boolean isFinish,
                                         String businessId, String businessType){
        if (StrUtil.isBlank(processInstanceId)){
            throw new CommonException("流程为空，审批进度初始化失败");
        }
        TcUser userBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (userBySession==null){
            throw new CommonException("用户不存在，审批进度初始化失败");
        }
        LambdaQueryWrapper<TcBusinessApproving> approvingLambdaQueryWrapper=new LambdaQueryWrapper<>();

        if (TaskStatusEnum.delete.equals(taskStatusEnum)){
            approvingLambdaQueryWrapper.clear();
            approvingLambdaQueryWrapper
                    .eq(TcBusinessApproving::getBusinessType,businessType)
                    .eq(TcBusinessApproving::getBusinessId,businessId);
            approvingMapper.delete(approvingLambdaQueryWrapper);
            FlowableMsgUtil.executeSend(title,content,businessType, businessId, null, FlowableConst.TASK_TIP_TYPE_ALL_DELETE,msgTypeList, jumpUrlParam);
            log.info("流程删除，删除全部消息");
        } else if (TaskStatusEnum.rollback.equals(taskStatusEnum)&&isFinish){
            approvingLambdaQueryWrapper.clear();
            approvingLambdaQueryWrapper
                    .eq(TcBusinessApproving::getBusinessType,businessType)
                    .eq(TcBusinessApproving::getBusinessId,businessId);
            approvingMapper.delete(approvingLambdaQueryWrapper);
            FlowableMsgUtil.executeSend(title,content,businessType, businessId, null, FlowableConst.TASK_TIP_TYPE_ALL_DELETE,msgTypeList, jumpUrlParam);
            log.info("流程回滚到开始节点，删除全部消息");
        } else {
            //获取审批任务人
            List<TaskInfoVo> taskInfoVos = flowableService.processActiveTaskWithoutData(processInstanceId);
            List<String> currentApproveUserIdList = taskInfoVos.stream().map(TaskInfoVo::getAssignee).collect(Collectors.toList());

            //获取之前审批任务人
            approvingLambdaQueryWrapper
                    .eq(TcBusinessApproving::getBusinessType,businessType)
                    .eq(TcBusinessApproving::getBusinessId,businessId);
            List<TcBusinessApproving> tcBusinessApprovings = approvingMapper.selectList(approvingLambdaQueryWrapper);
            List<String> preApproveUserIdList = tcBusinessApprovings.stream().map(TcBusinessApproving::getUserId).collect(Collectors.toList());

            //获取新增审批任务人
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
                    approving.setUserName(user.getNickName());
                    approvingMapper.insert(approving);
                }
                FlowableMsgUtil.executeSend(title,content,businessType,businessId,copyCurrentApproveUserIdList,FlowableConst.TASK_TIP_TYPE_UN_FINISH,msgTypeList, jumpUrlParam);
            }


            //获取需要删除的任务人员
            List<String> copyPreApproveUserIdList = BeanUtil.copyToList(preApproveUserIdList, String.class);
            copyPreApproveUserIdList.removeAll(currentApproveUserIdList);
            if (CollectionUtil.isNotEmpty(copyPreApproveUserIdList)){
                approvingLambdaQueryWrapper.clear();
                approvingLambdaQueryWrapper
                        .eq(TcBusinessApproving::getBusinessType,businessType)
                        .eq(TcBusinessApproving::getBusinessId,businessId)
                        .in(TcBusinessApproving::getUserId,copyPreApproveUserIdList);
                approvingMapper.delete(approvingLambdaQueryWrapper);

                if (TaskStatusEnum.rollback.equals(taskStatusEnum)||TaskStatusEnum.transfer.equals(taskStatusEnum)){
                    FlowableMsgUtil.executeSend(title,content,businessType, businessId, preApproveUserIdList, FlowableConst.TASK_TIP_TYPE_DELETE,msgTypeList, jumpUrlParam);
                }else {
                    FlowableMsgUtil.executeSend(title,content,businessType, businessId, copyPreApproveUserIdList, FlowableConst.TASK_TIP_TYPE_FINISH,msgTypeList, jumpUrlParam);
                }
            }
        }
    }


    public static void deleteHisByProcIdAndActIds(String processInstanceId,List<String> actIdList){
        QueryWrapper<Object> queryWrapper= Wrappers.query();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .in("ACT_ID_",actIdList);
        List<ActInst> hisActInst= approvingMapper.selectHisActInst(queryWrapper);
        if (CollectionUtil.isNotEmpty(hisActInst)){
            List<String> hisTaskIdList = hisActInst.stream().map(ActInst::getTaskId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
            List<String> idList = hisActInst.stream().map(ActInst::getId).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(hisTaskIdList)){
                queryWrapper.clear();
                queryWrapper.eq("PROC_INST_ID_",processInstanceId);
                queryWrapper.in("TASK_ID_",hisTaskIdList);
                approvingMapper.deleteHisIdentity(queryWrapper);

                queryWrapper.clear();
                queryWrapper.eq("PROC_INST_ID_",processInstanceId);
                queryWrapper.in("TASK_ID_",hisTaskIdList);
                approvingMapper.deleteHisVar(queryWrapper);

                queryWrapper.clear();
                queryWrapper
                        .eq("PROC_INST_ID_",processInstanceId)
                        .in("ID_",hisTaskIdList);
                approvingMapper.deleteHisTask(queryWrapper);
            }


            queryWrapper.clear();
            queryWrapper
                    .eq("PROC_INST_ID_",processInstanceId)
                    .in("ID_",idList);
            approvingMapper.deleteHisActInst(queryWrapper);
        }
    }

    public static void deleteRuByProcIdAndActIds(String processInstanceId,List<String> actIdList){
        QueryWrapper<Object> queryWrapper= Wrappers.query();

        queryWrapper.clear();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .in("ACT_ID_",actIdList);
        List<ActInst> ruActInst= approvingMapper.selectRuActInst(queryWrapper);
        if (CollectionUtil.isNotEmpty(ruActInst)){
            List<String> taskIdList = ruActInst.stream().map(ActInst::getTaskId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
            List<String> idList = ruActInst.stream().map(ActInst::getId).collect(Collectors.toList());

            if (CollectionUtil.isNotEmpty(taskIdList)){
                queryWrapper.clear();
                queryWrapper
                        .eq("PROC_INST_ID_",processInstanceId)
                        .in("TASK_ID_",taskIdList);
                approvingMapper.deleteRuVar(queryWrapper);

                queryWrapper.clear();
                queryWrapper
                        .eq("PROC_INST_ID_",processInstanceId)
                        .in("ID_",taskIdList);
                approvingMapper.deleteRuTask(queryWrapper);
            }

            queryWrapper.clear();
            queryWrapper
                    .eq("PROC_INST_ID_",processInstanceId)
                    .in("ID_",idList);
            approvingMapper.deleteRuActInst(queryWrapper);
        }

        queryWrapper.clear();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .in("ACT_ID_",actIdList);
        approvingMapper.deleteRuExecution(queryWrapper);
    }

    public static ProcessInstanceVo procInsConvertProcInsVo(ProcessInstance processInstance){
        ProcessInstanceVo processInstanceVo=new ProcessInstanceVo();
        ExecutionEntityImpl executionEntity= (ExecutionEntityImpl) processInstance;

        processInstanceVo.setEndTime(null);
        processInstanceVo.setDeleteReason(executionEntity.getDeleteReason());
        processInstanceVo.setDeleted(executionEntity.isDeleted());

        processInstanceVo.setId(processInstance.getId());
        processInstanceVo.setProcessVariables(processInstance.getProcessVariables());
        processInstanceVo.setBusinessKey(processInstance.getBusinessKey());
        processInstanceVo.setProcessDefinitionId(processInstance.getProcessDefinitionId());

        processInstanceVo.setSuspended(processInstance.isSuspended());
        processInstanceVo.setDescription(processInstance.getDescription());
        processInstanceVo.setName(processInstance.getName());
        processInstanceVo.setEnded(processInstance.isEnded());
        processInstanceVo.setDeploymentId(processInstance.getDeploymentId());
        processInstanceVo.setBusinessStatus(processInstance.getBusinessStatus());
        processInstanceVo.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
        processInstanceVo.setProcessDefinitionName(processInstance.getProcessDefinitionName());
        processInstanceVo.setProcessDefinitionVersion(processInstance.getProcessDefinitionVersion());
        processInstanceVo.setStartTime(processInstance.getStartTime());

        processInstanceVo.setHis(false);
        return processInstanceVo;
    }

    public static ProcessInstanceVo hisProcInsConvertProcInsVo(HistoricProcessInstance processInstance){
        ProcessInstanceVo processInstanceVo=new ProcessInstanceVo();
        HistoricProcessInstanceEntityImpl historicProcessInstanceEntity= (HistoricProcessInstanceEntityImpl) processInstance;

        processInstanceVo.setEndTime(historicProcessInstanceEntity.getEndTime());
        processInstanceVo.setDeleteReason(historicProcessInstanceEntity.getDeleteReason());
        processInstanceVo.setDeleted(historicProcessInstanceEntity.isDeleted());

        processInstanceVo.setId(processInstance.getId());
        processInstanceVo.setProcessVariables(processInstance.getProcessVariables());
        processInstanceVo.setBusinessKey(processInstance.getBusinessKey());
        processInstanceVo.setProcessDefinitionId(processInstance.getProcessDefinitionId());

        processInstanceVo.setSuspended(false);
        processInstanceVo.setDescription(processInstance.getDescription());
        processInstanceVo.setName(processInstance.getName());
        processInstanceVo.setEnded(historicProcessInstanceEntity.getEndTime() != null);
        processInstanceVo.setDeploymentId(processInstance.getDeploymentId());
        processInstanceVo.setBusinessStatus(processInstance.getBusinessStatus());
        processInstanceVo.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
        processInstanceVo.setProcessDefinitionName(processInstance.getProcessDefinitionName());
        processInstanceVo.setProcessDefinitionVersion(processInstance.getProcessDefinitionVersion());
        processInstanceVo.setStartTime(processInstance.getStartTime());

        processInstanceVo.setHis(true);
        return processInstanceVo;
    }

    public static TaskInfoVo taskConvertToTaskInfoVo(org.flowable.task.api.Task task){
        Map<String, Object> processVariables = task.getProcessVariables();
        Map<String, Object> taskLocalVariables = task.getTaskLocalVariables();
        Object status = taskLocalVariables.get(TaskVariableNameEnum.status.name());
        Object msg=taskLocalVariables.get(TaskVariableNameEnum.msg.name());
        Object actionTime = taskLocalVariables.get(TaskVariableNameEnum.actionTime.name());
        Object title = processVariables.get(ProcessVariableNameEnum.title.name());
        Object description = processVariables.get(ProcessVariableNameEnum.description.name());
        if (status==null){
            status="";
        }
        if (msg==null){
            msg="";
        }
        if (title==null){
            title="";
        }
        if (description==null){
            description="";
        }
        if (actionTime==null){
            actionTime = "";
        }
        TaskInfoVo taskInfoVo=new TaskInfoVo();
        BeanUtil.copyProperties(task,taskInfoVo);
        taskInfoVo.setStatus(status.toString());
        taskInfoVo.setMsg(msg.toString());
        taskInfoVo.setDescription(description.toString());
        taskInfoVo.setTitle(title.toString());
        taskInfoVo.setActionTime(actionTime.toString());
        taskInfoVo.setProcessVariables(task.getProcessVariables());
        taskInfoVo.setTaskLocalVariables(task.getTaskLocalVariables());

        taskInfoVo.setActName(taskInfoVo.getName());

        String assignee = taskInfoVo.getAssignee();
        TcUser user = tcUserMapper.selectById(assignee);
        if (user!=null){
            taskInfoVo.setName(user.getNickName());
        }

        return taskInfoVo;
    }

    public static TaskInfoVo hisTaskConvertToTaskInfoVo(HistoricTaskInstance task){
        Map<String, Object> processVariables = task.getProcessVariables();
        Map<String, Object> taskLocalVariables = task.getTaskLocalVariables();
        Object status = taskLocalVariables.get(TaskVariableNameEnum.status.name());
        Object msg=taskLocalVariables.get(TaskVariableNameEnum.msg.name());
        Object actionTime = taskLocalVariables.get(TaskVariableNameEnum.actionTime.name());
        Object title = processVariables.get(ProcessVariableNameEnum.title.name());
        Object description = processVariables.get(ProcessVariableNameEnum.description.name());
        Object businessNo= processVariables.get(ProcessVariableNameEnum.businessNo.name());
        if (status==null){
            status="";
        }
        if (msg==null){
            msg="";
        }
        if (title==null){
            title="";
        }
        if (description==null){
            description="";
        }
        if (businessNo==null){
            businessNo="";
        }
        if (actionTime==null){
            actionTime = "";
        }
        TaskInfoVo taskInfoVo=new TaskInfoVo();
        BeanUtil.copyProperties(task,taskInfoVo);
        taskInfoVo.setStatus(status.toString());
        taskInfoVo.setMsg(msg.toString());
        taskInfoVo.setDescription(description.toString());
        taskInfoVo.setTitle(title.toString());
        taskInfoVo.setBusinessNo(businessNo.toString());
        taskInfoVo.setActionTime(actionTime.toString());
        taskInfoVo.setProcessVariables(task.getProcessVariables());
        taskInfoVo.setTaskLocalVariables(task.getTaskLocalVariables());

        taskInfoVo.setActName(taskInfoVo.getName());

        String assignee = taskInfoVo.getAssignee();
        TcUser user = tcUserMapper.selectById(assignee);
        if (user!=null){
            taskInfoVo.setName(user.getNickName());
        }
        return taskInfoVo;
    }


    public static void getSiblingFlowNode(FlowElement sourceFlowElement, Map<String, FlowNode> siblingFlowNode){
        if (sourceFlowElement==null){
            return;
        }
        if (sourceFlowElement instanceof Gateway){
            return;
        }
        if (sourceFlowElement instanceof Task){
            Task task= (Task) sourceFlowElement;
            if (CollectionUtil.isEmpty(task.getIncomingFlows())){
                return;
            }
            List<FlowElement> upStreamSourceFlowElementList=new ArrayList<>();
            for (SequenceFlow sequenceFlow:task.getIncomingFlows()){
                FlowElement upStreamSourceFlowElement = sequenceFlow.getSourceFlowElement();
                upStreamSourceFlowElementList.add(upStreamSourceFlowElement);
            }
            for (FlowElement flowElement:upStreamSourceFlowElementList){
                if (flowElement instanceof FlowNode){
                    FlowNode flowNode= (FlowNode) flowElement;
                    for (SequenceFlow sequenceFlow:flowNode.getOutgoingFlows()){
                        FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
                        if (!sourceFlowElement.getId().equals(targetFlowElement.getId())){
                            siblingFlowNode.put(targetFlowElement.getId(), (FlowNode) targetFlowElement);
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取上游节点(用户节点、开始节点)
     */
    public static void getUpstreamFlowNode(List<FlowElement> sourceFlowElementList, Map<String, FlowNode> upstreamTask, boolean isRoot){
        if (CollectionUtil.isEmpty(sourceFlowElementList)){
            return;
        }
        for (FlowElement source:sourceFlowElementList){
            if (source instanceof Gateway){
                Gateway gateway= (Gateway) source;
                List<FlowElement> upStreamSourceFlowElementList=new ArrayList<>();
                for (SequenceFlow sequenceFlow:gateway.getIncomingFlows()){
                    FlowElement upStreamSourceFlowElement = sequenceFlow.getSourceFlowElement();
                    if (upStreamSourceFlowElement instanceof Gateway){
                        upStreamSourceFlowElementList.add(upStreamSourceFlowElement);
                    }else {
                        upstreamTask.put(upStreamSourceFlowElement.getId(), (FlowNode) upStreamSourceFlowElement);
                    }
                }
                getUpstreamFlowNode(upStreamSourceFlowElementList,upstreamTask,false);
            }
            if (source instanceof Task){
                if (isRoot){
                    //源节点则继续获取上游节点
                    Task task= (Task) source;
                    List<FlowElement> upStreamSourceFlowElementList=new ArrayList<>();
                    for (SequenceFlow sequenceFlow:task.getIncomingFlows()){
                        FlowElement upStreamSourceFlowElement = sequenceFlow.getSourceFlowElement();
                        upStreamSourceFlowElementList.add(upStreamSourceFlowElement);
                    }
                    getUpstreamFlowNode(upStreamSourceFlowElementList,upstreamTask,false);
                }else {
                    //非源节点
                    upstreamTask.put(source.getId(), (Task) source);
                }
            }
            if (source instanceof StartEvent){
                upstreamTask.put(source.getId(), (FlowNode) source);
            }
        }
    }
}
