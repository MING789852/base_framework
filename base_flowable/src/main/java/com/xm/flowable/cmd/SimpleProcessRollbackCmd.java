package com.xm.flowable.cmd;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
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
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.persistence.entity.*;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SimpleProcessRollbackCmd implements Command<RollBackResult>{

    private final ProcessInstance processInstance;

    private final RepositoryService repositoryService;

    private final RuntimeService runtimeService;

    private final HistoryService historyService;

    private final String executionId;

    public SimpleProcessRollbackCmd(ProcessInstance processInstance, String executionId) {
        if (StrUtil.isBlank(executionId)){
            throw new CommonException("回滚执行节点ID为空");
        }
        this.processInstance = processInstance;
        this.executionId = executionId;
        this.repositoryService = SpringBeanUtil.getBeanByClass(RepositoryService.class);
        this.runtimeService = SpringBeanUtil.getBeanByClass(RuntimeService.class);
        this.historyService = SpringBeanUtil.getBeanByClass(HistoryService.class);
    }


    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.SUPPORTS)
    public RollBackResult execute(CommandContext commandContext) {
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
                .executionId(executionId).singleResult();

        FlowElement sourceFlowElement=mainProcess.getFlowElement(execution.getActivityId());

        //==========上游历史节点处理============
        Map<String, FlowNode> upstreamFlowNode = new HashMap<>();
        FlowableUtil.getUpstreamFlowNode(Collections.singletonList(sourceFlowElement),upstreamFlowNode,true);
        List<FlowNode> upstreamFlowNodeList = new ArrayList<>(upstreamFlowNode.values());
        //判断上游节点是否为StartEvent
        Optional<FlowNode> startOptional = upstreamFlowNodeList.stream().filter(item -> item instanceof StartEvent).findFirst();
        if (startOptional.isPresent()){
            RollBackResult rollBackResult=new RollBackResult();
            rollBackResult.setFinish(true);
            return rollBackResult;
        }
        //处理上游节点
        Map<String,String> upstreamAssigneeMapping=new HashMap<>();
        if (CollectionUtil.isNotEmpty(upstreamFlowNodeList)){
            List<String> upStreamActId = upstreamFlowNodeList.stream().map(BaseElement::getId).collect(Collectors.toList());
            //获取上游节点的执行人
            List<HistoricTaskInstance> upStreamHistoricTask= historyService
                    .createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .taskDefinitionKeys(upStreamActId).list();
            upstreamAssigneeMapping=upStreamHistoricTask
                    .stream()
                    .collect(Collectors.toMap(TaskInfo::getTaskDefinitionKey,TaskInfo::getAssignee));
            //删除上游节点已审批
            FlowableUtil.deleteHisByProcIdAndActIds(processInstanceId,upStreamActId);
            FlowableUtil.deleteRuByProcIdAndActIds(processInstanceId,upStreamActId);
        }

        //处理兄弟节点
        Map<String, FlowNode> siblingFlowNode = new HashMap<>();
        FlowableUtil.getSiblingFlowNode(sourceFlowElement,siblingFlowNode);
        if (CollectionUtil.isNotEmpty(siblingFlowNode)){
            //获取兄弟节点的网关节点
            List<FlowElement> otherAll=new ArrayList<>();
            FlowNode node= (FlowNode) sourceFlowElement;
            Optional<FlowElement> gatWay = node.getOutgoingFlows().stream().findFirst().map(SequenceFlow::getTargetFlowElement);
            if (gatWay.isPresent()){
                FlowElement gatWayFlowElement = gatWay.get();
                otherAll.add(gatWayFlowElement);
            }
            //除自己以外的所有节点
            otherAll.addAll(siblingFlowNode.values());

            List<String> otherAllActIdList = otherAll.stream().map(BaseElement::getId).collect(Collectors.toList());
            FlowableUtil.deleteHisByProcIdAndActIds(processInstanceId,otherAllActIdList);
            FlowableUtil.deleteRuByProcIdAndActIds(processInstanceId,otherAllActIdList);
        }


        //获取回滚节点的执行id
        if (upstreamFlowNodeList.size()>1){
            //========处理回滚到上一个并行网关=======
            //获取上游网关节点
            Set<FlowElement> collect = upstreamFlowNodeList.stream().flatMap(item -> item.getIncomingFlows().stream())
                    .map(SequenceFlow::getSourceFlowElement).collect(Collectors.toSet());
            if (collect.size()!=1){
                throw new CommonException("无法回退");
            }
            FlowElement gatWayFlowElement = collect.stream().findFirst().get();
            //执行回滚
            runtimeService.createChangeActivityStateBuilder().processInstanceId(processInstanceId)
                    .moveExecutionToActivityId(executionId,gatWayFlowElement.getId()).changeState();
            //设置执行人(解决代办转发之后回滚流程，执行人未改变)
            RollBackResult rollBackResult=new RollBackResult();
            rollBackResult.setFinish(false);
            rollBackResult.setRollbackTargetActId(gatWayFlowElement.getId());
            rollBackResult.setUpstreamActIdAndAssigneeMapping(upstreamAssigneeMapping);
            rollBackResult.setUpstreamFlowNodeList(upstreamFlowNodeList);
            return rollBackResult;
        }else {
            //========处理普通节点回滚=======
            FlowElement task = upstreamFlowNodeList.get(0);
            runtimeService.createChangeActivityStateBuilder().processInstanceId(processInstanceId)
                    .moveExecutionToActivityId(executionId,task.getId()).changeState();

            RollBackResult rollBackResult=new RollBackResult();
            rollBackResult.setFinish(false);
            rollBackResult.setRollbackTargetActId(upstreamFlowNodeList.get(0).getId());
            rollBackResult.setUpstreamActIdAndAssigneeMapping(upstreamAssigneeMapping);
            rollBackResult.setUpstreamFlowNodeList(upstreamFlowNodeList);

            return rollBackResult;
        }
    }
}
