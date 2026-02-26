package com.xm.flowable.cmd;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.flowable.domain.dto.RollBackWithData;
import com.xm.util.bean.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SimpleProcessRollbackCmd implements Command<RollBackResult>{

    private final ProcessInstance processInstance;

    private final RepositoryService repositoryService;

    private final RuntimeService runtimeService;

    private final String executionId;

    private final ManagementService managementService;

    private final RollBackWithData rollBackWithData;

    public SimpleProcessRollbackCmd(ProcessInstance processInstance, String executionId, RollBackWithData rollBackWithData) {
        if (processInstance == null){
            throw new CommonException("回滚流程实例为空");
        }
        if (StrUtil.isBlank(executionId)){
            throw new CommonException("回滚执行节点ID为空");
        }
        this.processInstance = processInstance;
        this.executionId = executionId;
        this.repositoryService = SpringBeanUtil.getBeanByClass(RepositoryService.class);
        this.runtimeService = SpringBeanUtil.getBeanByClass(RuntimeService.class);
        this.managementService = SpringBeanUtil.getBeanByClass(ManagementService.class);
        this.rollBackWithData = rollBackWithData;
    }

    //获取目标节点
    private void recursionTargetBySource(List<FlowNode> targetFlowNodeList,FlowNode rootFlowNode){
        List<SequenceFlow> incomingFlows = rootFlowNode.getIncomingFlows();
        if (CollectionUtil.isNotEmpty(incomingFlows)){
            for (SequenceFlow sequenceFlow:incomingFlows){
                FlowElement sourceFlowElement = sequenceFlow.getSourceFlowElement();
                if (sourceFlowElement instanceof ParallelGateway){
                    ParallelGateway parallelGateway=(ParallelGateway) sourceFlowElement;
                    recursionTargetBySource(targetFlowNodeList,parallelGateway);
                }
                if (sourceFlowElement instanceof ExclusiveGateway){
                    ExclusiveGateway exclusiveGateway=(ExclusiveGateway) sourceFlowElement;
                    recursionTargetBySource(targetFlowNodeList,exclusiveGateway);
                }
                if (sourceFlowElement instanceof Task){
                    targetFlowNodeList.add((FlowNode) sourceFlowElement);
                }
                if (sourceFlowElement instanceof StartEvent){
                    targetFlowNodeList.add((FlowNode) sourceFlowElement);
                }
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.SUPPORTS)
    public RollBackResult execute(CommandContext commandContext) {
        // 获取流程定义
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        Process mainProcess = bpmnModel.getMainProcess();
        Collection<FlowElement> flowElements = mainProcess.getFlowElements();
        if (CollectionUtil.isEmpty(flowElements)){
            throw new CommonException("流程定义不存在");
        }
        //获取回滚节点模型
        String processInstanceId = processInstance.getId();
        Execution execution = runtimeService
                .createExecutionQuery()
                .processInstanceId(processInstanceId)
                .executionId(executionId).singleResult();
        FlowNode sourceFlowNode= (FlowNode) mainProcess.getFlowElement(execution.getActivityId());

        //获取目标节点
        List<FlowNode> targetFlowNodeList = new ArrayList<>();
        recursionTargetBySource(targetFlowNodeList,sourceFlowNode);
        if (CollectionUtil.isEmpty(targetFlowNodeList)){
            throw new CommonException("回滚节点不存在");
        }
        List<String> targetIdList = targetFlowNodeList.stream().map(FlowNode::getId).collect(Collectors.toList());

        SpecifyProcessRollbackCmd specifyProcessRollbackCmd=new SpecifyProcessRollbackCmd(processInstance,executionId, targetIdList, rollBackWithData);
        RollBackResult specifyRollBackResult = managementService.executeCommand(specifyProcessRollbackCmd);
        RollBackResult simpleRollBackResult=new RollBackResult();
        simpleRollBackResult.setFinish(specifyRollBackResult.isFinish());
        simpleRollBackResult.setActIdAndAssigneeMapping(specifyRollBackResult.getActIdAndAssigneeMapping());
        return simpleRollBackResult;
    }
}
