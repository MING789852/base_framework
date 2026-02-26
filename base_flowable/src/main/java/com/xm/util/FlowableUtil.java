package com.xm.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.mapper.TcUserMapper;
import com.xm.flowable.domain.entity.TcFlowableElements;
import com.xm.flowable.domain.query.ActInst;
import com.xm.flowable.domain.vo.*;
import com.xm.flowable.enums.*;
import com.xm.flowable.mapper.TcFlowableModelMapper;
import com.xm.flowable.service.FlowableService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.id.SnowIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.delegate.ActivityBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.persistence.entity.HistoricProcessInstanceEntityImpl;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FlowableUtil {
    private static TcFlowableModelMapper modelMapper;

    private static TcUserMapper tcUserMapper;

    private static ExpressionManager expressionManager;

    @Autowired
    public  void setApprovingMapper(TcFlowableModelMapper approvingMapper) {
        FlowableUtil.modelMapper = approvingMapper;
    }

    @Autowired
    public  void setExpressionManager(ExpressionManager expressionManager) {
        FlowableUtil.expressionManager = expressionManager;
    }

    @Autowired
    public  void setTcUserMapper(TcUserMapper tcUserMapper) {
        FlowableUtil.tcUserMapper = tcUserMapper;
    }

    public static String generaId(){
        return "fs_"+ SnowIdUtil.getSnowId();
    }


    public static boolean validExpression(String expression){
        try {
            expressionManager.createExpression(expression);
            return true;
        } catch (FlowableException e) {
            return false;
        }
    }

    public static int updateGeByteArray(String deploymentId,byte[] bpmnBytes){
        if (bpmnBytes==null){
            throw new CommonException("【updateGeByteArray】bpmnBytes不能为空");
        }
        if (StrUtil.isBlank(deploymentId)){
            throw new CommonException("【updateGeByteArray】deploymentId不能为空");
        }
        return modelMapper.updateGeByteArray(deploymentId,bpmnBytes);
    }

    public static List<ActInst>  selectRuActInst(QueryWrapper<Object> queryWrapper){
        return modelMapper.selectRuActInst(queryWrapper);
    }


    public static void deleteHisByProcIdAndActIds(String processInstanceId,List<String> actIdList){
        QueryWrapper<Object> queryWrapper= Wrappers.query();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .in("ACT_ID_",actIdList);
        List<ActInst> hisActInst= modelMapper.selectHisActInst(queryWrapper);
        if (CollectionUtil.isNotEmpty(hisActInst)){
            List<String> hisTaskIdList = hisActInst.stream().map(ActInst::getTaskId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(hisTaskIdList)){
                queryWrapper.clear();
                queryWrapper.eq("PROC_INST_ID_",processInstanceId);
                queryWrapper.in("TASK_ID_",hisTaskIdList);
                modelMapper.deleteHisIdentity(queryWrapper);

                queryWrapper.clear();
                queryWrapper.eq("PROC_INST_ID_",processInstanceId);
                queryWrapper.in("TASK_ID_",hisTaskIdList);
                modelMapper.deleteHisVar(queryWrapper);

                queryWrapper.clear();
                queryWrapper
                        .eq("PROC_INST_ID_",processInstanceId)
                        .in("ID_",hisTaskIdList);
                modelMapper.deleteHisTask(queryWrapper);
            }
        }
        queryWrapper.clear();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .in("ACT_ID_",actIdList);
        modelMapper.deleteHisActInst(queryWrapper);
    }

    public static void deleteRuByProcIdAndExecutionIds(String processInstanceId,List<String> executionIdList){
        QueryWrapper<Object> queryWrapper= Wrappers.query();

        queryWrapper.clear();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .in("EXECUTION_ID_",executionIdList);
        modelMapper.deleteRuVar(queryWrapper);

        queryWrapper.clear();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .in("EXECUTION_ID_",executionIdList);
        modelMapper.deleteRuTask(queryWrapper);

        queryWrapper.clear();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .in("EXECUTION_ID_",executionIdList);
        modelMapper.deleteRuActInst(queryWrapper);

        queryWrapper.clear();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .in("ID_",executionIdList);
        modelMapper.deleteRuExecution(queryWrapper);

    }

    public static void deleteRuByProcIdAndActIds(String processInstanceId,List<String> actIdList){
        QueryWrapper<Object> queryWrapper= Wrappers.query();
        queryWrapper.clear();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .in("ACT_ID_",actIdList);
        List<ActInst> ruActInst= modelMapper.selectRuActInst(queryWrapper);
        if (CollectionUtil.isNotEmpty(ruActInst)){
            List<String> taskIdList = ruActInst.stream().map(ActInst::getTaskId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(taskIdList)){
                queryWrapper.clear();
                queryWrapper
                        .eq("PROC_INST_ID_",processInstanceId)
                        .in("TASK_ID_",taskIdList);
                modelMapper.deleteRuVar(queryWrapper);

                queryWrapper.clear();
                queryWrapper
                        .eq("PROC_INST_ID_",processInstanceId)
                        .in("ID_",taskIdList);
                modelMapper.deleteRuTask(queryWrapper);
            }
        }

        queryWrapper.clear();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .in("ACT_ID_",actIdList);
        modelMapper.deleteRuActInst(queryWrapper);

        queryWrapper.clear();
        queryWrapper
                .eq("PROC_INST_ID_",processInstanceId)
                .in("ACT_ID_",actIdList);
        modelMapper.deleteRuExecution(queryWrapper);
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

        //设置businessType
        Map<String, Object> processVariables = processInstanceVo.getProcessVariables();
        if (CollectionUtil.isNotEmpty(processVariables)){
            String businessType = FlowableUtil.getMapVariable(processVariables, ProcessVariableNameEnum.businessType.name());
            processInstanceVo.setBusinessType(businessType);
        }

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

        //设置businessType
        Map<String, Object> processVariables = processInstanceVo.getProcessVariables();
        if (CollectionUtil.isNotEmpty(processVariables)){
            String businessType = FlowableUtil.getMapVariable(processVariables, ProcessVariableNameEnum.businessType.name());
            processInstanceVo.setBusinessType(businessType);
        }

        processInstanceVo.setHis(true);
        return processInstanceVo;
    }

    public static <T> T getMapVariable(Map<String, Object> processVariables,String  key){
        Object object = processVariables.get(key);
        if (object==null){
            return null;
        }else {
            @SuppressWarnings("unchecked")
            T t=(T) object;
            return t;
        }
    }

    public static boolean isMultiInstanceTaskName(String actName){
        if (actName.contains("或签")||actName.contains("会签")){
            return true;
        }else {
            return false;
        }
    }

    public static TaskInfoVo taskConvertToTaskInfoVo(org.flowable.task.api.Task task){
        Map<String, Object> taskLocalVariables = task.getTaskLocalVariables();
        if (taskLocalVariables==null){
            taskLocalVariables=new HashMap<>();
        }
        String status = Optional.ofNullable(taskLocalVariables.get(TaskVariableNameEnum.status.name()))
                .map(Object::toString).orElse( "");
        String msg=Optional.ofNullable(taskLocalVariables.get(TaskVariableNameEnum.msg.name()))
                .map(Object::toString).orElse( "");
        String actionTime = Optional.ofNullable(taskLocalVariables.get(TaskVariableNameEnum.actionTime.name()))
                .map(Object::toString).orElse( "");
        TaskInfoVo taskInfoVo=new TaskInfoVo();
        BeanUtil.copyProperties(task,taskInfoVo);
        taskInfoVo.setStatus(status);
        taskInfoVo.setMsg(msg);
        taskInfoVo.setActionTime(actionTime);
        taskInfoVo.setProcessVariables(task.getProcessVariables());
        taskInfoVo.setTaskLocalVariables(taskLocalVariables);

        taskInfoVo.setActName(taskInfoVo.getName());

        String assignee = taskInfoVo.getAssignee();
        TcUser user = tcUserMapper.selectById(assignee);
        if (user!=null){
            String actName = taskInfoVo.getActName();
            if (StrUtil.isBlank(actName)){
                taskInfoVo.setName(user.getNickName());
            }else {
                if (isMultiInstanceTaskName(actName)){
                    taskInfoVo.setName(StrUtil.format("{}【{}】",user.getNickName(),actName));
                }else {
                    taskInfoVo.setName(user.getNickName());
                }
            }
        }
        taskInfoVo.setHis(false);

        return taskInfoVo;
    }

    public static TaskInfoVo hisTaskConvertToTaskInfoVo(HistoricTaskInstance task){
        Map<String, Object> taskLocalVariables = task.getTaskLocalVariables();
        if (taskLocalVariables==null){
            taskLocalVariables=new HashMap<>();
        }
        String status = Optional.ofNullable(taskLocalVariables.get(TaskVariableNameEnum.status.name()))
                .map(Object::toString).orElse( "");
        String msg=Optional.ofNullable(taskLocalVariables.get(TaskVariableNameEnum.msg.name()))
                .map(Object::toString).orElse( "");
        String actionTime = Optional.ofNullable(taskLocalVariables.get(TaskVariableNameEnum.actionTime.name()))
                .map(Object::toString).orElse( "");
        TaskInfoVo taskInfoVo=new TaskInfoVo();
        BeanUtil.copyProperties(task,taskInfoVo);
        taskInfoVo.setStatus(status);
        taskInfoVo.setMsg(msg);
        taskInfoVo.setActionTime(actionTime);
        taskInfoVo.setProcessVariables(task.getProcessVariables());
        taskInfoVo.setTaskLocalVariables(taskLocalVariables);

        taskInfoVo.setActName(taskInfoVo.getName());

        String assignee = taskInfoVo.getAssignee();
        TcUser user = tcUserMapper.selectById(assignee);
        if (user!=null){
            String actName = taskInfoVo.getActName();
            if (StrUtil.isBlank(actName)){
                taskInfoVo.setName(user.getNickName());
            }else {
                if (isMultiInstanceTaskName(actName)){
                    taskInfoVo.setName(StrUtil.format("{}【{}】",user.getNickName(),actName));
                }else {
                    taskInfoVo.setName(user.getNickName());
                }
            }
        }
        taskInfoVo.setHis(true);

        return taskInfoVo;
    }


    //获取下游节点(不包括自己)
    public static void getAllDownstreamFlowNode(List<FlowElement> sourceFlowElementList, Map<String, FlowElement> downstreamFlowNode){
        if (CollectionUtil.isEmpty(sourceFlowElementList)){
            return;
        }
        for (FlowElement source:sourceFlowElementList){
            if (source instanceof EndEvent){
                downstreamFlowNode.put(source.getId(), (FlowNode) source);
            }else {
                List<FlowElement> downstreamFlowElementList=new ArrayList<>();
                FlowNode flowNode= (FlowNode) source;
                for (SequenceFlow sequenceFlow:flowNode.getOutgoingFlows()){
                    FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
                    downstreamFlowNode.put(sequenceFlow.getId(),sequenceFlow);
                    downstreamFlowNode.put(targetFlowElement.getId(), targetFlowElement);
                    downstreamFlowElementList.add(targetFlowElement);
                }
                getAllDownstreamFlowNode(downstreamFlowElementList,downstreamFlowNode);
            }
        }
    }

    //获取上游节点(不包括自己)
    public static void getAllUpstreamFlowNode(List<FlowElement> sourceFlowElementList, Map<String, FlowNode> upstreamFlowNode){
        if (CollectionUtil.isEmpty(sourceFlowElementList)){
            return;
        }
        for (FlowElement source:sourceFlowElementList){
            if (source instanceof StartEvent){
                upstreamFlowNode.put(source.getId(), (FlowNode) source);
            }else {
                List<FlowElement> upstreamFlowElementList=new ArrayList<>();
                FlowNode flowNode= (FlowNode) source;
                List<SequenceFlow> incomingFlows = flowNode.getIncomingFlows();
                for (SequenceFlow sequenceFlow:incomingFlows){
                    FlowElement sourceFlowElement = sequenceFlow.getSourceFlowElement();
                    upstreamFlowNode.put(sourceFlowElement.getId(), (FlowNode) sourceFlowElement);
                    upstreamFlowElementList.add(sourceFlowElement);
                }
                getAllUpstreamFlowNode(upstreamFlowElementList,upstreamFlowNode);
            }
        }
    }


//    public static ExecutionEntity initExecution(ExecutionEntityManager executionEntityManager
//            , String activityId,String processDefinitionId,String processInstanceId){
//        ExecutionEntity execution = executionEntityManager.create();
//        execution.setId(idGenerator.getNextId());
//        execution.setProcessDefinitionId(processDefinitionId);
//        execution.setParentId(processInstanceId);
//        execution.setProcessInstanceId(processInstanceId);
//        execution.setRootProcessInstanceId(processInstanceId);
//        execution.setActive(false);
//        ((ExecutionEntityImpl) execution).setActivityId(activityId);
//        execution.setSuspensionState(1);
//        execution.setRevision(0);
//        execution.setStartTime(new Date());
//        execution.setTenantId(null);
//        ((ExecutionEntityImpl) execution).setIsActive(false);
//        ((ExecutionEntityImpl) execution).setIsConcurrent(false);
//        ((ExecutionEntityImpl) execution).setIsScope(false);
//        ((ExecutionEntityImpl) execution).setIsMultiInstanceRoot(false);
//        ((ExecutionEntityImpl) execution).setCountEnabled(true);
//
//        return execution;
//    }

    public static void createGateWayExecution(ExecutionEntityManager executionEntityManager
            ,String processInstanceId, ParallelGateway parallelGateway){

        ExecutionEntity parentExecutionEntity = executionEntityManager.findById(processInstanceId);
        ExecutionEntity childExecution = executionEntityManager.createChildExecution(parentExecutionEntity);
        childExecution.setCurrentFlowElement(parallelGateway);
        ActivityBehavior activityBehavior = (ActivityBehavior) parallelGateway.getBehavior();
        activityBehavior.execute(childExecution);
    }


    public static List<TcFlowableElements> convertEdgeElementToFlowableElement(List<EdgeElement> edges, String processDefinitionKey, String processDefinitionName){
        List<TcFlowableElements> flowableElements=new ArrayList<>();
        if (CollectionUtil.isEmpty(edges)){
            return flowableElements;
        }
        for (EdgeElement edgeElement:edges){
            TcFlowableElements elements=new TcFlowableElements();

            elements.setElementId(edgeElement.getId());
            elements.setElementType(edgeElement.getType());
            elements.setSourceNodeId(edgeElement.getSourceNodeId());
            elements.setTargetNodeId(edgeElement.getTargetNodeId());
            NodeText text = edgeElement.getText();
            if (text!=null){
                elements.setTextX(text.getX());
                elements.setTextY(text.getY());
                elements.setElementName(text.getValue());
            }
            //设置条件
            ElementProp properties = edgeElement.getProperties();
            if (properties!=null){
                String conditionalExpression = properties.getConditionalExpression();
                elements.setContent1(conditionalExpression);
            }
            //存储边pointsList
            List<EdgePoint> pointsList = edgeElement.getPointsList();
            EdgePoint startPoint = edgeElement.getStartPoint();
            EdgePoint endPoint = edgeElement.getEndPoint();
            if (CollectionUtil.isNotEmpty(pointsList)){
                EdgePointConfig edgePointConfig = new EdgePointConfig();
                edgePointConfig.setStartPoint(startPoint);
                edgePointConfig.setEndPoint(endPoint);
                edgePointConfig.setPointsList(pointsList);
                elements.setContent2(JSONUtil.toJsonStr(edgePointConfig));
            }
            elements.setProcessDefinitionKey(processDefinitionKey);
            elements.setProcessDefinitionName(processDefinitionName);
            flowableElements.add(elements);
        }
        return flowableElements;
    }

    public static List<EdgeElement> convertFlowableElementToEdgeElement(List<TcFlowableElements> flowableElementsList){
        List<EdgeElement> edgeElementList=new ArrayList<>();
        if (CollectionUtil.isEmpty(flowableElementsList)){
            return edgeElementList;
        }
        for (TcFlowableElements flowableElements:flowableElementsList){
            if (FlowableElementTypeEnum.sequenceFlow.equals(flowableElements.getElementType())){
                EdgeElement edgeElement=new EdgeElement();
                edgeElement.setType(flowableElements.getElementType());
                edgeElement.setId(flowableElements.getElementId());
                edgeElement.setTargetNodeId(flowableElements.getTargetNodeId());
                edgeElement.setSourceNodeId(flowableElements.getSourceNodeId());

                if (StrUtil.isNotBlank(flowableElements.getElementName())){
                    NodeText nodeText=new NodeText();
                    nodeText.setX(flowableElements.getTextX());
                    nodeText.setY(flowableElements.getTextY());
                    nodeText.setValue(flowableElements.getElementName());
                    edgeElement.setText(nodeText);
                }

                //设置边条件表达式
                ElementProp elementProp = new ElementProp();
                String content1 = flowableElements.getContent1();
                elementProp.setConditionalExpression(content1);
                edgeElement.setProperties(elementProp);
                edgeElementList.add(edgeElement);

                //设置边pointsList
                String content2 = flowableElements.getContent2();
                if (StrUtil.isNotBlank(content2)){
                    EdgePointConfig edgePointConfig = JSONUtil.toBean(content2, EdgePointConfig.class);
                    edgeElement.setPointsList(edgePointConfig.getPointsList());
                    edgeElement.setStartPoint(edgePointConfig.getStartPoint());
                    edgeElement.setEndPoint(edgePointConfig.getEndPoint());
                }else {
                    edgeElement.setPointsList(new ArrayList<>());
                }
            }
        }
        return edgeElementList;
    }

    public static List<TcFlowableElements> convertNodeElementToFlowableElement(List<NodeElement> nodes,String processDefinitionKey,String processDefinitionName){
        List<TcFlowableElements> flowableElements=new ArrayList<>();
        if (CollectionUtil.isEmpty(nodes)){
            return flowableElements;
        }
        for (NodeElement nodeElement:nodes){
            TcFlowableElements elements=new TcFlowableElements();

            elements.setElementId(nodeElement.getId());
            elements.setElementType(nodeElement.getType());
            elements.setElementX(nodeElement.getX());
            elements.setElementY(nodeElement.getY());
            NodeText text = nodeElement.getText();
            if (text!=null){
                elements.setTextX(text.getX());
                elements.setTextY(text.getY());
                elements.setElementName(text.getValue());
            }
            ElementProp properties = nodeElement.getProperties();
            if (properties!=null){
                elements.setAssignee(properties.getAssignee());
                elements.setAssigneeName(properties.getAssigneeName());

                if (properties.getUserTaskApprovalType()==null){
                    elements.setContent1(UserTaskApprovalTypeEnum.single.name());
                }else {
                    elements.setContent1(properties.getUserTaskApprovalType());
                }
                List<String> candidateUsers = properties.getCandidateUsers();
                if (CollectionUtil.isNotEmpty(candidateUsers)){
                    elements.setContent2(JSONUtil.toJsonStr(candidateUsers));
                }
                if (properties.getApprovalCount()!=null){
                    elements.setContent3(properties.getApprovalCount().toString());
                }
                if (properties.getCategory()!=null){
                    elements.setContent4(properties.getCategory());
                }
                elements.setContent5(properties.getUserConfigActionType());
                elements.setContent6(properties.getCustomVar());
            }
            elements.setProcessDefinitionKey(processDefinitionKey);
            elements.setProcessDefinitionName(processDefinitionName);

            flowableElements.add(elements);
        }
        return flowableElements;
    }

    public static List<NodeElement> convertFlowableElementToNodeElement(List<TcFlowableElements> flowableElementsList){
        List<NodeElement> nodeElementList=new ArrayList<>();
        if (CollectionUtil.isEmpty(flowableElementsList)){
            return nodeElementList;
        }
        for (TcFlowableElements flowableElements:flowableElementsList) {
            if (!FlowableElementTypeEnum.sequenceFlow.equals(flowableElements.getElementType())) {
                NodeElement nodeElement=new NodeElement();
                nodeElement.setId(flowableElements.getElementId());
                nodeElement.setX(flowableElements.getElementX());
                nodeElement.setY(flowableElements.getElementY());
                nodeElement.setType(flowableElements.getElementType());
                if (StrUtil.isNotBlank(flowableElements.getElementName())){
                    NodeText nodeText=new NodeText();
                    nodeText.setX(flowableElements.getTextX());
                    nodeText.setY(flowableElements.getTextY());
                    nodeText.setValue(flowableElements.getElementName());
                    nodeElement.setText(nodeText);
                }
                ElementProp elementProp =new ElementProp();
                elementProp.setAssignee(flowableElements.getAssignee());
                elementProp.setAssigneeName(flowableElements.getAssigneeName());

                String content1 = flowableElements.getContent1();
                if (StrUtil.isBlank(content1)){
                    elementProp.setUserTaskApprovalType(UserTaskApprovalTypeEnum.single.name());
                }else {
                    elementProp.setUserTaskApprovalType(content1);
                }
                String content2 = flowableElements.getContent2();
                if (StrUtil.isNotBlank(content2)){
                    elementProp.setCandidateUsers(JSONUtil.toList(content2, String.class));
                }else {
                    elementProp.setCandidateUsers(new ArrayList<>());
                }
                String content3 = flowableElements.getContent3();
                if (StrUtil.isNotBlank(content3)){
                    elementProp.setApprovalCount(Integer.parseInt(content3));
                }
                elementProp.setCategory(flowableElements.getContent4());

                String content5 = flowableElements.getContent5();
                String content6 = flowableElements.getContent6();
                elementProp.setUserConfigActionType(content5);
                elementProp.setCustomVar(content6);
                nodeElement.setProperties(elementProp);

                nodeElementList.add(nodeElement);
            }
        }
        return nodeElementList;
    }


    public static Map<String,String> getMapDataFromFlowableElement(FlowElement flowElement){
        Map<String,String> map=new HashMap<>();
        if (flowElement==null){
            return map;
        }
        Map<String, List<ExtensionElement>> extensionElementMap = flowElement.getExtensionElements();
        if (CollectionUtil.isEmpty(extensionElementMap)){
            return map;
        }
        for (Map.Entry<String, List<ExtensionElement>> entry : extensionElementMap.entrySet()){
            List<ExtensionElement> extensionElements = entry.getValue();
            String key = entry.getKey();
            if (CollectionUtil.isEmpty(extensionElements)){
                continue;
            }
            ExtensionElement extensionElement = extensionElements.get(0);
            map.put(key,extensionElement.getElementText());
        }
        return map;
    }

    public static void putMapDataToFlowableElement(FlowElement flowElement,Map<String,String>  map){
        if (CollectionUtil.isEmpty(map)||flowElement==null){
            return;
        }
        Map<String, List<ExtensionElement>> extensionElementMap = flowElement.getExtensionElements();
        if (extensionElementMap==null){
            extensionElementMap=new HashMap<>();
            flowElement.setExtensionElements(extensionElementMap);
        }
        for (Map.Entry<String, String> entry : map.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            if (StrUtil.isBlank(value)||StrUtil.isBlank(key)){
                continue;
            }
            List<ExtensionElement> extensionElements = extensionElementMap.computeIfAbsent(key, k -> new ArrayList<>());
            ExtensionElement extensionElement = new ExtensionElement();
            extensionElement.setNamespace("http://flowable.org/bpmn");
            extensionElement.setName(key);
            extensionElement.setElementText(value);
            extensionElements.add(extensionElement);
        }
    }


    public static void handleUserTaskApprovalType(Process mainProcess,UserTask userTask, NodeElement nodeElement){
        ElementProp properties = nodeElement.getProperties();
        if (properties==null){
            throw new CommonException("用户节点properties为空");
        }
        String userTaskApprovalType = properties.getUserTaskApprovalType();
        userTask.setId(nodeElement.getId());
        if (StrUtil.isNotBlank(properties.getCategory())){
            userTask.setCategory(properties.getCategory());
        }
        NodeText text = nodeElement.getText();
        if (text==null){
            throw new CommonException("用户节点名称不能为空");
        }
        if (StrUtil.isBlank(text.getValue())){
            throw new CommonException("用户节点名称不能为空");
        }
        userTask.setName(text.getValue());
        //删除静态人员变量
        String fieldName=userTask.getId()+"_"+"candidateTaskUsers";
        FlowElement staticDataElement = mainProcess.getFlowElement(fieldName);
        if (staticDataElement!=null){
            mainProcess.removeFlowElement(fieldName);
        }
        if (userTaskApprovalType==null|| UserTaskApprovalTypeEnum.single.name().equals(userTaskApprovalType)){
            String assignee;
            //判断是否自定义人员变量
            if (UserConfigActionTypeEnum.customVar.name().equals(properties.getUserConfigActionType())){
                assignee=properties.getCustomVar();
                if (StrUtil.isBlank(assignee)){
                    throw new CommonException("用户节点审批人变量不能为空");
                }
                if (!(assignee.startsWith("${")&&assignee.endsWith("}"))){
                    throw new CommonException("用户节点审批人变量格式错误,应为${xxx}");
                }
            }else {
                assignee=properties.getAssignee();
                if (StrUtil.isBlank(assignee)){
                    throw new CommonException("用户节点审批人不能为空");
                }
            }
            userTask.setAssignee(assignee);
        }else {
            MultiInstanceLoopCharacteristics multiInstance = new MultiInstanceLoopCharacteristics();
            if (UserConfigActionTypeEnum.customVar.name().equals(properties.getUserConfigActionType())){
                String candidateUsersVar=properties.getCustomVar();
                if (StrUtil.isBlank(candidateUsersVar)){
                    throw new CommonException("候选用户列表变量不能为空");
                }
                if (!(candidateUsersVar.startsWith("${")&&candidateUsersVar.endsWith("}"))){
                    throw new CommonException("候选用户列表变量格式错误,应为${xxx}");
                }
                multiInstance.setInputDataItem(candidateUsersVar);
            }else {
                // 固定人员
                List<String> candidateUsers = properties.getCandidateUsers();
                if (CollectionUtil.isEmpty(candidateUsers)||
                        candidateUsers.stream().anyMatch(s -> s == null || s.trim().isEmpty())){
                    throw new CommonException("候选用户列表不能为空且不得包含空值");
                }
                //设置静态人员变量
                JsonDataObject jsonDataObject=new JsonDataObject();
                jsonDataObject.setId(fieldName);
                jsonDataObject.setName(fieldName);
                jsonDataObject.setValue(candidateUsers);
                mainProcess.addFlowElement(jsonDataObject);
                //转换人员变量
                multiInstance.setInputDataItem(StrUtil.format("${baseBeanFunction.parseJsonToList({})}",fieldName));
            }
            //设置完成条件
            if (UserTaskApprovalTypeEnum.all.name().equals(userTaskApprovalType)){
                multiInstance.setCompletionCondition("${nrOfCompletedInstances==nrOfInstances}");
            }
            if (UserTaskApprovalTypeEnum.or.name().equals(userTaskApprovalType)){
                Integer approvalCount = properties.getApprovalCount();
                if (approvalCount<=0){
                    throw new CommonException("审批数量不能小于等于0");
                }
                multiInstance.setCompletionCondition(StrUtil.format("${nrOfCompletedInstances>={}}",approvalCount));
            }
            //并行模式
            multiInstance.setSequential(false);
            multiInstance.setElementVariable("approver");
            //审批人员设置
            userTask.setAssignee("${approver}");
            userTask.setLoopCharacteristics(multiInstance);
        }
    }

    public static List<TaskInfoVo> currentUserValidateCanApprove(String processInstanceId){
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        FlowableService beanByClass = SpringBeanUtil.getBeanByClass(FlowableService.class);
        List<TaskInfoVo> taskInfoVos = beanByClass.getTaskByUserAndProcessId(currentLoginUserBySession, processInstanceId);
        if (CollectionUtil.isEmpty(taskInfoVos)){
            return new ArrayList<>();
        }
        return taskInfoVos;
    }
}
