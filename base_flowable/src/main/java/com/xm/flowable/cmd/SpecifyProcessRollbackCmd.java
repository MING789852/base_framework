package com.xm.flowable.cmd;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.flowable.domain.dto.RollBackWithData;
import com.xm.flowable.domain.query.ActInst;
import com.xm.flowable.domain.vo.TaskInfoVo;
import com.xm.flowable.enums.ProcessVariableNameEnum;
import com.xm.flowable.enums.TaskStatusEnum;
import com.xm.util.FlowableUtil;
import com.xm.util.bean.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SpecifyProcessRollbackCmd implements Command<RollBackResult> {

    private final ProcessInstance processInstance;

    private final RepositoryService repositoryService;

    private final RuntimeService runtimeService;

    private final HistoryService historyService;

    private final String sourceExecutionId;

    private final List<String> targetActivityIdList;

    private final RollBackWithData rollBackWithData;


    public SpecifyProcessRollbackCmd(ProcessInstance processInstance, String sourceExecutionId, List<String> targetActivityIdList, RollBackWithData rollBackWithData)  {
        if (processInstance == null) {
            throw new CommonException("回滚流程实例为空");
        }
        if (StrUtil.isBlank(sourceExecutionId)){
            throw new CommonException("回滚执行节点ID为空");
        }
        if (CollectionUtil.isEmpty(targetActivityIdList)){
            throw new CommonException("回滚目标节点ID为空");
        }
        this.processInstance = processInstance;
        this.sourceExecutionId = sourceExecutionId;
        this.targetActivityIdList = targetActivityIdList;
        this.repositoryService = SpringBeanUtil.getBeanByClass(RepositoryService.class);
        this.runtimeService = SpringBeanUtil.getBeanByClass(RuntimeService.class);
        this.historyService = SpringBeanUtil.getBeanByClass(HistoryService.class);
        this.rollBackWithData = rollBackWithData;
    }

    @Override
    public RollBackResult execute(CommandContext commandContext) {

        RollBackResult specifyRollBackResult=new RollBackResult();

        // 校验流程是否结束
        if (processInstance == null||processInstance.isSuspended()||processInstance.isEnded()) {
            throw new CommonException("流程已结束或已挂起，无法执行撤回操作");
        }
        String processInstanceId = processInstance.getId();
        // 获取流程定义
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        Process mainProcess = bpmnModel.getMainProcess();
        Collection<FlowElement> flowElements = mainProcess.getFlowElements();
        if (CollectionUtil.isEmpty(flowElements)){
            throw new CommonException("流程定义不存在");
        }


        //获取回滚节点模型
        Execution execution = runtimeService
                .createExecutionQuery()
                .processInstanceId(processInstanceId)
                .executionId(sourceExecutionId).singleResult();
        FlowElement sourceFlowElement=mainProcess.getFlowElement(execution.getActivityId());


        //获取源节点所有上游节点
        Map<String,FlowNode> upstreamFlowNode = new HashMap<>();
        FlowableUtil.getAllUpstreamFlowNode(Collections.singletonList(sourceFlowElement),upstreamFlowNode);

        List<FlowElement> targetFlowElementList = new ArrayList<>();
        for (String targetActivityId : targetActivityIdList){
            FlowElement targetFlowElement=mainProcess.getFlowElement(targetActivityId);
            if (targetFlowElement==null){
                throw new CommonException("目标节点定义不存在");
            }
            //处理目标节点
            //目标节点是开始节点，则直接结束返回
            if (targetFlowElement instanceof StartEvent){
                specifyRollBackResult.setFinish(true);
                return specifyRollBackResult;
            }
            if (!upstreamFlowNode.containsKey(targetActivityId)){
                throw new CommonException("回滚目标节点非源节点的上游");
            }
            //判断目标节点的下游节点是否包含在目标节点集合中（目标节点存在串在一起的情况）
            Map<String, FlowElement> downstreamFlowNode = new HashMap<>();
            FlowableUtil.getAllDownstreamFlowNode(Collections.singletonList(targetFlowElement), downstreamFlowNode);

            if (CollectionUtil.isNotEmpty(downstreamFlowNode)){
                List<FlowElement> findDownstreamFlowNode = downstreamFlowNode.entrySet().stream()
                        .filter(item -> targetActivityIdList.contains(item.getKey()))
                        .map(Map.Entry::getValue).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(findDownstreamFlowNode)){
                    throw new CommonException(StrUtil.format("回滚目标节点【{}】不能包含目标节点的下游节点【{}】",
                            targetFlowElement.getName(),findDownstreamFlowNode.stream().map(FlowElement::getName).collect(Collectors.joining(","))));
                }
            }
            targetFlowElementList.add(targetFlowElement);
        }

        if (sourceFlowElement==null){
            throw new CommonException("源节点定义不存在");
        }

        if (targetActivityIdList.contains(sourceFlowElement.getId())){
            throw new CommonException("回滚节点不能与目标节点相同");
        }

        //获取历史的执行人
        List<HistoricTaskInstance> historicTaskInstanceList= historyService
                .createHistoricTaskInstanceQuery()
                .finished()
                .taskWithoutDeleteReason()
                .processInstanceId(processInstanceId).list();
//        Map<String,String> actIdAndAssigneeMapping=historicTaskInstanceList
//                .stream()
//                .collect(Collectors.toMap(TaskInfo::getTaskDefinitionKey,TaskInfo::getAssignee));
        Map<String,String> actIdAndAssigneeMapping=historicTaskInstanceList
                .stream()
                .collect(Collectors.groupingBy(TaskInfo::getTaskDefinitionKey,
                        Collectors.collectingAndThen(Collectors.toList(),item->item.get(0).getAssignee())));

        //从回滚的目标节点开始找到所有下游节点
        Map<String, FlowElement> downstreamFlowNode = new HashMap<>();
        FlowableUtil.getAllDownstreamFlowNode(targetFlowElementList,downstreamFlowNode);

        /*
        删除正在审批记录
         */
        //处理多实例
        List<Execution> list = runtimeService
                .createExecutionQuery()
                .activityId(sourceFlowElement.getId())
                .processInstanceId(processInstanceId)
                .list();
        if (CollectionUtil.isNotEmpty(list)){
            List<Execution> unsourceExecutionList =
                    list.stream().filter(item -> !sourceExecutionId.equals(item.getId())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(unsourceExecutionList)){
                List<String> unsourceExecutionIdList = unsourceExecutionList.stream().map(Execution::getId).collect(Collectors.toList());
                FlowableUtil.deleteRuByProcIdAndExecutionIds(processInstanceId,unsourceExecutionIdList);
            }
        }
        //其它已审批和待审批节点
        List<String> deleteActIdList = downstreamFlowNode.keySet().stream()
                .filter(item -> !item.equals(sourceFlowElement.getId())).collect(Collectors.toList());
        deleteActIdList.addAll(targetActivityIdList);
        //删除正在审批
        FlowableUtil.deleteRuByProcIdAndActIds(processInstanceId,deleteActIdList);
        //删除已审批记录
        FlowableUtil.deleteHisByProcIdAndActIds(processInstanceId,deleteActIdList);

        if (rollBackWithData!=null){
            Task task = rollBackWithData.getTask();
            boolean withRecord = rollBackWithData.isWithRecord();
            String rollBackRecordMsg = rollBackWithData.getRollBackRecordMsg();
            //记录回滚数据
            if (withRecord){
                Map<String, Object> processVariables = processInstance.getProcessVariables();
                Object rollBackRecordMsgObj = processVariables.get(ProcessVariableNameEnum.rollBackRecordMsg.name());
                List<TaskInfoVo> rollBackRecordMsgList;
                if (rollBackRecordMsgObj!=null){
                    rollBackRecordMsgList= JSONUtil.toList(rollBackRecordMsgObj.toString(),TaskInfoVo.class);
                }else {
                    rollBackRecordMsgList=new ArrayList<>();
                }

                Date now = new Date();
                TaskInfoVo taskInfoVo=FlowableUtil.taskConvertToTaskInfoVo(task);
                taskInfoVo.setProcessVariables(null);
                taskInfoVo.setTaskLocalVariables(null);
                taskInfoVo.setCreateTime(now);
                taskInfoVo.setActionTime(DateUtil.formatDateTime(now));
                taskInfoVo.setStatus(TaskStatusEnum.rollBackRecordMsg.name());
                taskInfoVo.setMsg(rollBackRecordMsg);
                rollBackRecordMsgList.add(taskInfoVo);

                runtimeService.setVariable(processInstanceId,ProcessVariableNameEnum.rollBackRecordMsg.name(),JSONUtil.toJsonStr(rollBackRecordMsgList));
            }
        }
        //回滚
        runtimeService.createChangeActivityStateBuilder().processInstanceId(processInstanceId)
                .moveSingleExecutionToActivityIds(sourceExecutionId,targetActivityIdList).changeState();


        QueryWrapper<Object> queryWrapper= new QueryWrapper<>();
        queryWrapper.clear();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .isNotNull("END_TIME_")
                .isNull("DELETE_REASON_");
        List<ActInst> actInstList = FlowableUtil.selectRuActInst(queryWrapper);
        Set<String> activityIdList = actInstList.stream().map(ActInst::getActId).collect(Collectors.toSet());

        //补充目标节点下游网关节点
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);
        List<FlowElement> downstreamList = new ArrayList<>(downstreamFlowNode.values());
        List<FlowElement> downstreamGatewayList = downstreamList.stream().filter(item -> item instanceof ParallelGateway).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(downstreamGatewayList)){
            for (FlowElement item : downstreamGatewayList){
                ParallelGateway parallelGateway=(ParallelGateway) item;
                List<SequenceFlow> incomingFlows = parallelGateway.getIncomingFlows();
                if (CollectionUtil.isNotEmpty(incomingFlows)){
                    for (SequenceFlow sequenceFlow : incomingFlows){
                        FlowElement  sourceToGateway= sequenceFlow.getSourceFlowElement();
                        if (activityIdList.contains(sourceToGateway.getId())
                                &&!sourceToGateway.getId().equals(sourceFlowElement.getId())
                                &&!targetActivityIdList.contains(sourceToGateway.getId())){
                            FlowableUtil.createGateWayExecution(executionEntityManager,processInstanceId,parallelGateway);
                        }
                    }
                }
            }
        }
        //补充目标网关节点
        for (FlowElement item : targetFlowElementList){
            if (item instanceof ParallelGateway){
                ParallelGateway parallelGateway=(ParallelGateway) item;
                List<SequenceFlow> incomingFlows = parallelGateway.getIncomingFlows();
                if (incomingFlows.size()==1){
                    continue;
                }
                for (int i=1;i<=incomingFlows.size()-1;i++){
                    FlowableUtil.createGateWayExecution(executionEntityManager,processInstanceId,parallelGateway);
                }
            }
        }

        specifyRollBackResult.setActIdAndAssigneeMapping(actIdAndAssigneeMapping);
        specifyRollBackResult.setFinish(false);

        return specifyRollBackResult;
    }
}
