package com.xm.flowable.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.service.TcUserService;
import com.xm.flowable.domain.dto.ChangeModel;
import com.xm.flowable.domain.entity.TcFlowableElements;
import com.xm.flowable.domain.entity.TcFlowableModel;
import com.xm.flowable.domain.query.ProcessInstancePageQuery;
import com.xm.flowable.domain.res.FlowableMsgCreate;
import com.xm.flowable.domain.vo.*;
import com.xm.flowable.enums.*;
import com.xm.flowable.listener.FlowableTaskListener;
import com.xm.flowable.mapper.TcFlowableElementsMapper;
import com.xm.flowable.mapper.TcFlowableModelMapper;
import com.xm.flowable.service.FlowableModelService;
import com.xm.flowable.service.FlowableService;
import com.xm.module.core.params.QueryData;
import com.xm.util.FlowableConst;
import com.xm.util.FlowableMsgUtil;
import com.xm.util.FlowableUtil;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.sql.SqlGenerateUtil;
import com.xm.util.valid.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.impl.persistence.deploy.DeploymentManager;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Service
public class FlowableModelServiceImpl implements FlowableModelService {
    private final TcFlowableElementsMapper flowableElementsMapper;
    private final TcFlowableModelMapper flowableModelMapper;
    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final TcUserService userService;
    private final HistoryService historyService;
    private final DynamicBpmnService dynamicBpmnService;


    private SequenceFlow convertEdgeToFlowElement(EdgeElement edgeElement){
        SequenceFlow sequenceFlow = new SequenceFlow(edgeElement.getSourceNodeId(), edgeElement.getTargetNodeId());
        NodeText text = edgeElement.getText();
        if (text!=null&&StrUtil.isNotBlank(text.getValue())){
            sequenceFlow.setName(text.getValue());
        }
        String id = edgeElement.getId();
        sequenceFlow.setId(id);
        ElementProp properties = edgeElement.getProperties();
        if (properties!=null){
            String conditionalExpression = properties.getConditionalExpression();
            if (StrUtil.isNotBlank(conditionalExpression)){
                boolean b = FlowableUtil.validExpression(conditionalExpression);
                if (!b){
                    throw new CommonException(StrUtil.format("条件表达式[{}]格式错误",conditionalExpression));
                }
                sequenceFlow.setConditionExpression(conditionalExpression);
            }
        }
        return sequenceFlow;
    }

    private FlowElement convertNoedToFlowElement(Process mainProcess,NodeElement nodeElement){
        if (FlowableElementTypeEnum.startEvent.equals(nodeElement.getType())){
            StartEvent startEvent = new StartEvent();
            startEvent.setId(nodeElement.getId());
            return startEvent;
        }
        if (FlowableElementTypeEnum.exclusiveGateway.equals(nodeElement.getType())){
            ExclusiveGateway exclusiveGateway=new ExclusiveGateway();
            exclusiveGateway.setId(nodeElement.getId());
            return exclusiveGateway;
        }
        if (FlowableElementTypeEnum.parallelGateway.equals(nodeElement.getType())){
            ParallelGateway parallelGateway=new ParallelGateway();
            parallelGateway.setId(nodeElement.getId());
            return parallelGateway;
        }
        if (FlowableElementTypeEnum.userTask.equals(nodeElement.getType())){
            UserTask userTask=new UserTask();
            FlowableUtil.handleUserTaskApprovalType(mainProcess,userTask,nodeElement);
            return userTask;
        }
        if (FlowableElementTypeEnum.serviceTask.equals(nodeElement.getType())){
            ElementProp properties = nodeElement.getProperties();
            NodeText text = nodeElement.getText();
            ServiceTask serviceTask=new ServiceTask();
            serviceTask.setId(nodeElement.getId());
            serviceTask.setName(text.getValue());
            String type = properties.getUserTaskApprovalType();
            String category = properties.getCategory();
            if (StrUtil.isBlank(type)){
                throw new CommonException("服务任务节点类型为空");
            }
            if (category==null){
                category= SnowIdUtil.getSnowId();
            }
            Map<String,String> result=new HashMap<>();
            if (ServiceTaskActionTypeEnum.cc.name().equals(type)){
                List<String> candidateUsers = Optional.ofNullable(properties.getCandidateUsers()).orElse(new ArrayList<>());
                result.put(ServiceTaskVariableNameEnum.ccUserList.name(), JSONUtil.toJsonStr(candidateUsers));
                result.put(ServiceTaskVariableNameEnum.category.name(),category);
                serviceTask.setImplementation(StrUtil.format("${serviceTaskFunction.execute(execution,'{}','{}')}",type,category));
            }
            if (ServiceTaskActionTypeEnum.customExpression.name().equals(type)){
                String customVar = properties.getCustomVar();
                serviceTask.setImplementation(customVar);
            }
            FlowableUtil.putMapDataToFlowableElement(serviceTask,result);
            serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
            return serviceTask;
        }
        if (FlowableElementTypeEnum.endEvent.equals(nodeElement.getType())){
            EndEvent endEvent=new EndEvent();
            endEvent.setId(nodeElement.getId());
            return endEvent;
        }
        throw new CommonException("不支持的节点类型");
    }

    private void createBpmnModel(FlowableModelVo flowableModelVo){
        List<EdgeElement> edges = flowableModelVo.getEdges();
        List<NodeElement> nodes = flowableModelVo.getNodes();

        BpmnModel bpmnModel=new BpmnModel();
        Process mainProcess = new Process();
        mainProcess.setId(flowableModelVo.getProcessDefinitionKey());
        mainProcess.setName(flowableModelVo.getProcessDefinitionName());
        bpmnModel.addProcess(mainProcess);

        //创建连线，设置条件表达式
        for (EdgeElement edgeElement:edges){
            if (FlowableElementTypeEnum.sequenceFlow.equals(edgeElement.getType())){
                SequenceFlow sequenceFlow = convertEdgeToFlowElement(edgeElement);
                mainProcess.addFlowElement(sequenceFlow);
            }
        }
        Map<String, List<EdgeElement>> outSequenceFlow = edges.stream().collect(Collectors.groupingBy(EdgeElement::getSourceNodeId));
        Map<String, List<EdgeElement>> inSequenceFlow = edges.stream().collect(Collectors.groupingBy(EdgeElement::getTargetNodeId));


        int startEventNum=0;
        int endEventNum=0;
        for (NodeElement nodeElement:nodes){
            List<EdgeElement> outEdgeElements = outSequenceFlow.get(nodeElement.getId());
            List<EdgeElement> inEdgeElements = inSequenceFlow.get(nodeElement.getId());
            if (FlowableElementTypeEnum.startEvent.equals(nodeElement.getType())){
                if (CollectionUtil.isNotEmpty(inEdgeElements)){
                    throw new CommonException("开始节点入度必须为0");
                }
                if (CollectionUtil.isEmpty(outEdgeElements)){
                    throw new CommonException("开始节点出度不能为0");
                }
                startEventNum++;
            }
            if (FlowableElementTypeEnum.exclusiveGateway.equals(nodeElement.getType())){
                if (CollectionUtil.isEmpty(outEdgeElements)){
                    throw new CommonException("网关节点出度不能为0");
                }
                if (CollectionUtil.isEmpty(inEdgeElements)){
                    throw new CommonException("网关节点入度不能为0");
                }
            }
            if (FlowableElementTypeEnum.parallelGateway.equals(nodeElement.getType())){
                if (CollectionUtil.isEmpty(outEdgeElements)){
                    throw new CommonException("网关节点出度不能为0");
                }
                if (CollectionUtil.isEmpty(inEdgeElements)){
                    throw new CommonException("网关节点入度不能为0");
                }
            }
            if (FlowableElementTypeEnum.userTask.equals(nodeElement.getType())){
                ElementProp properties = nodeElement.getProperties();
                if (properties==null){
                    throw new CommonException("用户节点properties为空");
                }
                String assigneeName = properties.getAssigneeName();
                if (CollectionUtil.isEmpty(outEdgeElements)){
                    throw new CommonException(StrUtil.format("用户节点[{}]出度不能为0",assigneeName));
                }
                if (CollectionUtil.isEmpty(inEdgeElements)){
                    throw new CommonException(StrUtil.format("用户节点[{}]入度不能为0",assigneeName));
                }
            }
            if (FlowableElementTypeEnum.serviceTask.equals(nodeElement.getType())){
                ElementProp properties = nodeElement.getProperties();
                String type = properties.getUserTaskApprovalType();
                if (StrUtil.isBlank(type)){
                    throw new CommonException("服务任务节点类型为空");
                }
            }
            if (FlowableElementTypeEnum.endEvent.equals(nodeElement.getType())){
                if (CollectionUtil.isNotEmpty(outEdgeElements)){
                    throw new CommonException("结束节点出度必须为0");
                }
                if (CollectionUtil.isEmpty(inEdgeElements)){
                    throw new CommonException("结束节点入度不能为0");
                }
                endEventNum++;
            }
            FlowElement flowElement = convertNoedToFlowElement(mainProcess, nodeElement);
            mainProcess.addFlowElement(flowElement);
        }
        if (startEventNum!=1){
            throw new CommonException("开始节点只允许存在一个");
        }
        if (endEventNum!=1){
            throw new CommonException("结束节点只允许存在一个");
        }
        //判断重复
        Deployment database = repositoryService.createDeploymentQuery()
                .deploymentKey(flowableModelVo.getProcessDefinitionKey()).singleResult();
        if (database!=null){
            String msg= StrUtil.format("流程定义KEY->{}流程已存在,请重命名",flowableModelVo.getProcessDefinitionKey());
            throw new CommonException(msg);
        }

        //自动调整布局
        new BpmnAutoLayout(bpmnModel).execute();
        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment
                .addBpmnModel(StrUtil.format("{}.bpmn",flowableModelVo.getProcessDefinitionKey()),bpmnModel)
                .key(flowableModelVo.getProcessDefinitionKey())
                .name(flowableModelVo.getProcessDefinitionName())
                .deploy();
        log.info("模型部署,模型id->{},模型key->{},模型名称->{}",deploy.getId(),deploy.getKey(),deploy.getName());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String initUnRecordModel() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().latestVersion().list();
        FlowableModelService beanByClass = SpringBeanUtil.getBeanByClass(FlowableModelService.class);
        if (CollectionUtil.isNotEmpty(list)){
            for (ProcessDefinition processDefinition:list){
                beanByClass.recordModel(processDefinition.getKey(),processDefinition.getName());
            }
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordModel(String processDefinitionKey, String processDefinitionName) {
        LambdaQueryWrapper<TcFlowableModel> modelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(TcFlowableModel::getProcessDefinitionKey,processDefinitionKey);
        Long count = flowableModelMapper.selectCount(modelLambdaQueryWrapper);
        if (count.intValue()==0){
            TcFlowableModel flowableModel=new TcFlowableModel();
            flowableModel.setProcessDefinitionName(processDefinitionName);
            flowableModel.setProcessDefinitionKey(processDefinitionKey);
            flowableModel.setCreateUser("system");
            flowableModel.setCreateDate(new Date());
            flowableModelMapper.insert(flowableModel);

            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(processDefinitionKey)
                    .latestVersion()
                    .singleResult();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
            Map<String, GraphicInfo> locationMap = bpmnModel.getLocationMap();
            Process mainProcess = bpmnModel.getMainProcess();

            BigDecimal eventTextHeight=new BigDecimal(40);
            for (FlowElement flowElement:mainProcess.getFlowElements()){
                TcFlowableElements flowableElements=new TcFlowableElements();
                if (flowElement instanceof SequenceFlow){
                    SequenceFlow sequenceFlow= (SequenceFlow) flowElement;

                    flowableElements.setElementId(sequenceFlow.getId());
                    flowableElements.setElementType(FlowableElementTypeEnum.sequenceFlow);
                    flowableElements.setTargetNodeId(sequenceFlow.getTargetRef());
                    flowableElements.setSourceNodeId(sequenceFlow.getSourceRef());
                    flowableElements.setProcessDefinitionName(processDefinitionName);
                    flowableElements.setProcessDefinitionKey(processDefinitionKey);

                    flowableElementsMapper.insert(flowableElements);
                }else {
                    GraphicInfo graphicInfo = locationMap.get(flowElement.getId());
                    flowableElements.setElementX(BigDecimal.valueOf(graphicInfo.getX()));
                    flowableElements.setElementY(BigDecimal.valueOf(graphicInfo.getY()));

                    if (flowElement instanceof ParallelGateway){
                        flowableElements.setElementType(FlowableElementTypeEnum.parallelGateway);
                    }
                    if (flowElement instanceof UserTask){
                        flowableElements.setElementType(FlowableElementTypeEnum.userTask);
                        UserTask userTask= (UserTask) flowElement;
                        flowableElements.setContent1(userTask.getCategory());
                        flowableElements.setAssignee(userTask.getAssignee());
                        flowableElements.setAssigneeName(userTask.getName());
                        flowableElements.setElementName(userTask.getName());

                        flowableElements.setTextX(flowableElements.getElementX());
                        flowableElements.setTextY(flowableElements.getElementY());
                    }
                    if (flowElement instanceof StartEvent){
                        flowableElements.setElementName("开始");
                        flowableElements.setTextX(flowableElements.getElementX());
                        flowableElements.setTextY(flowableElements.getElementY().add(eventTextHeight));
                        flowableElements.setElementType(FlowableElementTypeEnum.startEvent);
                    }
                    if (flowElement instanceof EndEvent){
                        flowableElements.setElementName("结束");
                        flowableElements.setTextX(flowableElements.getElementX());
                        flowableElements.setTextY(flowableElements.getElementY().add(eventTextHeight));
                        flowableElements.setElementType(FlowableElementTypeEnum.endEvent);
                    }

                    flowableElements.setElementId(flowElement.getId());
                    flowableElements.setProcessDefinitionKey(processDefinitionKey);
                    flowableElements.setProcessDefinitionName(processDefinitionName);

                    flowableElementsMapper.insert(flowableElements);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveModel(FlowableModelVo flowableModelVo) {
        ValidationUtils.validateEntity(flowableModelVo);
        List<EdgeElement> edges = flowableModelVo.getEdges();
        List<NodeElement> nodes = flowableModelVo.getNodes();
        if (CollectionUtil.isEmpty(edges)){
            throw new CommonException("边为空");
        }
        if (CollectionUtil.isEmpty(nodes)){
            throw new CommonException("节点为空");
        }
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，无权限操作");
        }
        //判断模型是否存在
        LambdaQueryWrapper<TcFlowableModel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcFlowableModel::getProcessDefinitionKey,flowableModelVo.getProcessDefinitionKey());
        TcFlowableModel dataBase = flowableModelMapper.selectOne(lambdaQueryWrapper);
        if (dataBase==null){
            //存储流程节点数据
            List<TcFlowableElements> edgeFlowableElement = FlowableUtil.convertEdgeElementToFlowableElement(edges,
                    flowableModelVo.getProcessDefinitionKey(),flowableModelVo.getProcessDefinitionName());
            List<TcFlowableElements> nodeFlowableElement = FlowableUtil.convertNodeElementToFlowableElement(nodes,
                    flowableModelVo.getProcessDefinitionKey(),flowableModelVo.getProcessDefinitionName());
            List<TcFlowableElements> all=new ArrayList<>();
            all.addAll(edgeFlowableElement);
            all.addAll(nodeFlowableElement);
            for (TcFlowableElements elements:all) {
                flowableElementsMapper.insert(elements);
            }
            //存储流程数据
            flowableModelVo.setCreateDate(new Date());
            flowableModelVo.setCreateUser(currentLoginUserBySession.getNickName());
            flowableModelMapper.insert(flowableModelVo);

            //创建流程模型
            createBpmnModel(flowableModelVo);
        }else {
            FlowableService flowableService = SpringBeanUtil.getBeanByClass(FlowableService.class);
            if (flowableService.existProcessInstanceByProcessDefinitionKey(flowableModelVo.getProcessDefinitionKey())) {
//                //更新布局
//                LambdaQueryWrapper<TcFlowableElements> elementsLambdaQueryWrapper=new LambdaQueryWrapper<>();
//                for (NodeElement nodeElement:nodes){
//                    elementsLambdaQueryWrapper.clear();
//                    elementsLambdaQueryWrapper
//                            .eq(TcFlowableElements::getProcessDefinitionKey,flowableModelVo.getProcessDefinitionKey())
//                            .eq(TcFlowableElements::getElementId,nodeElement.getId());
//                    TcFlowableElements elements=new TcFlowableElements();
//                    NodeText text = nodeElement.getText();
//                    if (text!=null){
//                        elements.setTextX(text.getX());
//                        elements.setTextY(text.getY());
//                    }
//                    elements.setElementX(nodeElement.getX());
//                    elements.setElementY(nodeElement.getY());
//                    flowableElementsMapper.update(elements,elementsLambdaQueryWrapper);
//                }
                String msg = StrUtil.format("流程模型key->{},存在流程实例，无法修改", flowableModelVo.getProcessDefinitionKey());
                throw new CommonException(msg);
            }else {
                //删除实际模型
                flowableService.deleteDeploymentByKey(dataBase.getProcessDefinitionKey());
                //更新流程节点数据(先删除后新增)
                List<TcFlowableElements> edgeFlowableElement = FlowableUtil.convertEdgeElementToFlowableElement(edges,
                        flowableModelVo.getProcessDefinitionKey(),flowableModelVo.getProcessDefinitionName());
                List<TcFlowableElements> nodeFlowableElement = FlowableUtil.convertNodeElementToFlowableElement(nodes,
                        flowableModelVo.getProcessDefinitionKey(),flowableModelVo.getProcessDefinitionName());
                List<TcFlowableElements> all=new ArrayList<>();
                all.addAll(edgeFlowableElement);
                all.addAll(nodeFlowableElement);

                LambdaQueryWrapper<TcFlowableElements> elementsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                elementsLambdaQueryWrapper
                        .eq(TcFlowableElements::getProcessDefinitionKey,flowableModelVo.getProcessDefinitionKey());
                flowableElementsMapper.delete(elementsLambdaQueryWrapper);
                flowableElementsMapper.insert(all);
                //更新流程数据
                LambdaQueryWrapper<TcFlowableModel> modelLambdaQueryWrapper=new LambdaQueryWrapper<>();
                modelLambdaQueryWrapper.eq(TcFlowableModel::getProcessDefinitionKey,flowableModelVo.getProcessDefinitionKey());
                flowableModelVo.setCreateDate(new Date());
                flowableModelVo.setCreateUser(currentLoginUserBySession.getNickName());
                flowableModelMapper.update(flowableModelVo,modelLambdaQueryWrapper);
                //创建流程模型
                createBpmnModel(flowableModelVo);
            }
        }

        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String changeModelNode(ChangeModel changeModel) {
        NodeElement nodeElement= changeModel.getNode();
        String processDefinitionKey = changeModel.getProcessDefinitionKey();
        if (nodeElement==null){
            throw new CommonException("节点为空");
        }
        if (StrUtil.isBlank(processDefinitionKey)){
            throw new CommonException("流程模型key不能为空");
        }

        LambdaQueryWrapper<TcFlowableModel> modelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(TcFlowableModel::getProcessDefinitionKey,processDefinitionKey);
        TcFlowableModel dataBase = flowableModelMapper.selectOne(modelLambdaQueryWrapper);
        if (dataBase==null){
            throw new CommonException("流程模型不存在");
        }
        ElementProp properties = nodeElement.getProperties();
        String id = nodeElement.getId();
        String assigneeName = properties.getAssigneeName();
        if (StrUtil.isBlank(id)){
            throw new CommonException("节点ID不能为空");
        }
        if (StrUtil.isBlank(assigneeName)){
            throw new CommonException("节点名称不能为空");
        }
        String assignee = properties.getAssignee();
        if (StrUtil.isBlank(assignee)){
            throw new CommonException("节点审批人不能为空");
        }
        //更新节点定义
        List<TcFlowableElements> updateList=FlowableUtil
                .convertNodeElementToFlowableElement(Collections.singletonList(nodeElement),
                        dataBase.getProcessDefinitionKey(),dataBase.getProcessDefinitionName());
        LambdaQueryWrapper<TcFlowableElements> elementsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        elementsLambdaQueryWrapper
                .eq(TcFlowableElements::getProcessDefinitionKey,processDefinitionKey)
                .eq(TcFlowableElements::getElementId,nodeElement.getId());
        int count=flowableElementsMapper.update(updateList.get(0),elementsLambdaQueryWrapper);
        if (count==0){
            throw new CommonException("节点不存在");
        }
        //修改流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();
        String processDefinitionId = processDefinition.getId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        // 一个BPMN文件可能包含多个流程，但通常只有一个，所以一般取第一个
        Process mainProcess = bpmnModel.getMainProcess();
        FlowElement newFlowElement = convertNoedToFlowElement(mainProcess, nodeElement);


        /*
          (推荐)方法一、重新部署并生成新版本、旧版本实例需要单独修改
         */
//        // 1. 先保存所有与该节点相关的连线
//        List<SequenceFlow> oldSequenceFlows = mainProcess.getFlowElements()
//                .stream()
//                .filter(item -> item instanceof SequenceFlow)
//                .map(item -> (SequenceFlow) item).collect(Collectors.toList());
//        // 2. 暂时移除这些连线
//        for (SequenceFlow flow : oldSequenceFlows) {
//            mainProcess.removeFlowElement(flow.getId());
//        }
//        // 3. 删除并重新创建节点
//        mainProcess.removeFlowElement(newFlowElement.getId());
//        mainProcess.addFlowElement(newFlowElement);
//        // 4. 添加连线
//        for (SequenceFlow oldSequenceFlow : oldSequenceFlows) {
//            mainProcess.addFlowElement(oldSequenceFlow);
//        }
//        repositoryService.createDeployment()
//                .addBpmnModel(StrUtil.format("{}.bpmn",dataBase.getProcessDefinitionKey()),bpmnModel)
//                .key(dataBase.getProcessDefinitionKey())
//                .name(dataBase.getProcessDefinitionName())
//                .deploy();
        /*
          方法二、刷新act_ge_bytearray表中的xml和流程图、刷新内存缓存
         */
        mainProcess.removeFlowElement(newFlowElement.getId());
        mainProcess.addFlowElement(newFlowElement);

        BpmnAutoLayout bpmnAutoLayout = new BpmnAutoLayout(bpmnModel);
        bpmnAutoLayout.execute();
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        String deploymentId = processDefinition.getDeploymentId();

        //修改数据库模型
        int updateCount = FlowableUtil.updateGeByteArray(deploymentId, bpmnBytes);
        log.info("【updateGeByteArray】更新数量->{}",updateCount);

        //刷新缓存模型
        SpringProcessEngineConfiguration configuration=SpringBeanUtil.getBeanByClass(SpringProcessEngineConfiguration.class);
        DeploymentManager deploymentManager = configuration.getDeploymentManager();
        deploymentManager.getProcessDefinitionCache().remove(processDefinitionId);

        return "操作成功";
    }

    @Override
    public String changeModelEdge(ChangeModel changeModel) {
        String processDefinitionKey = changeModel.getProcessDefinitionKey();
        EdgeElement edgeElement = changeModel.getEdge();
        if (edgeElement==null){
            throw new CommonException("边为空");
        }
        if (StrUtil.isBlank(processDefinitionKey)){
            throw new CommonException("流程模型key不能为空");
        }
        LambdaQueryWrapper<TcFlowableModel> modelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(TcFlowableModel::getProcessDefinitionKey,processDefinitionKey);
        TcFlowableModel dataBase = flowableModelMapper.selectOne(modelLambdaQueryWrapper);
        if (dataBase==null){
            throw new CommonException("流程模型不存在");
        }
        String sourceNodeId = edgeElement.getSourceNodeId();
        if (StrUtil.isBlank(sourceNodeId)){
            throw new CommonException("源节点ID不能为空");
        }
        String targetNodeId = edgeElement.getTargetNodeId();
        if (StrUtil.isBlank(targetNodeId)){
            throw new CommonException("目标节点ID不能为空");
        }

        //更新节点定义
        List<TcFlowableElements> updateList=FlowableUtil
                .convertEdgeElementToFlowableElement(Collections.singletonList(edgeElement),
                        dataBase.getProcessDefinitionKey(),dataBase.getProcessDefinitionName());
        LambdaQueryWrapper<TcFlowableElements> elementsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        elementsLambdaQueryWrapper
                .eq(TcFlowableElements::getProcessDefinitionKey,processDefinitionKey)
                .eq(TcFlowableElements::getElementId,edgeElement.getId());
        int count=flowableElementsMapper.update(updateList.get(0),elementsLambdaQueryWrapper);
        if (count==0){
            throw new CommonException("边不存在");
        }

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();
        String processDefinitionId = processDefinition.getId();

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        // 一个BPMN文件可能包含多个流程，但通常只有一个，所以一般取第一个
        Process mainProcess = bpmnModel.getMainProcess();
        FlowElement newFlowElement = convertEdgeToFlowElement(edgeElement);

        /*
          (推荐)方法一、使用dynamicBpmnService动态修改
         */
//        //获取流程定义信息对象
//        ObjectNode infoNode = dynamicBpmnService.getProcessDefinitionInfo(processDefinitionId);
//        dynamicBpmnService.changeSequenceFlowCondition(edgeElement.getId(),properties.getConditionalExpression(),infoNode);
//        //保存到数据库！
//        dynamicBpmnService.saveProcessDefinitionInfo(processDefinitionId, infoNode);

        mainProcess.removeFlowElement(newFlowElement.getId());
        mainProcess.addFlowElement(newFlowElement);

        BpmnAutoLayout bpmnAutoLayout = new BpmnAutoLayout(bpmnModel);
        bpmnAutoLayout.execute();
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        String deploymentId = processDefinition.getDeploymentId();

        //修改数据库模型
        int updateCount = FlowableUtil.updateGeByteArray(deploymentId, bpmnBytes);
        log.info("【updateGeByteArray】更新数量->{}",updateCount);

        //刷新缓存模型
        SpringProcessEngineConfiguration configuration=SpringBeanUtil.getBeanByClass(SpringProcessEngineConfiguration.class);
        DeploymentManager deploymentManager = configuration.getDeploymentManager();
        deploymentManager.getProcessDefinitionCache().remove(processDefinitionId);

        return "操作成功";
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public String reSendApproveMsg(List<ProcessInstanceVo> processInstanceVoList) {
        if (CollectionUtil.isEmpty(processInstanceVoList)){
            throw new CommonException("请选择需要重新发送的流程");
        }
        try {
            FlowableService flowableService = SpringUtil.getBean(FlowableService.class);
            for (ProcessInstanceVo processInstanceVo : processInstanceVoList){
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                        .active()
                        .processInstanceId(processInstanceVo.getId())
                        .includeProcessVariables()
                        .singleResult();
                if (processInstance==null){
                    continue;
                }
                Map<String, Object> processVariables = processInstance.getProcessVariables();
                processInstanceVo.setProcessVariables(processVariables);
                String businessKey=FlowableUtil.getMapVariable(processVariables, ProcessVariableNameEnum.businessKey.name());
                String businessType=FlowableUtil.getMapVariable(processVariables, ProcessVariableNameEnum.businessType.name());
                String listenerClassName = FlowableUtil.getMapVariable(processVariables, ProcessVariableNameEnum.listener.name());
                String creatorId = FlowableUtil.getMapVariable(processVariables, ProcessVariableNameEnum.creatorId.name());
                FlowableTaskListener beanByClass = (FlowableTaskListener) SpringBeanUtil.getBeanByClass(Class.forName(listenerClassName));

                //获取当前审批任务人
                List<TaskInfoVo> taskInfoVos = flowableService.processActiveTaskWithoutData(processInstance.getId());
                List<String> currentApproveUserIdList = taskInfoVos.stream().map(TaskInfoVo::getAssignee).collect(Collectors.toList());

                List<FlowableMsgCreate> msglist = beanByClass.getMsglist(processInstanceVo,
                        currentApproveUserIdList, null, TaskStatusEnum.success, false, null);
                for (FlowableMsgCreate flowableMsgCreate : msglist){
                    FlowableMsgUtil.executeSend(creatorId,businessType, businessKey, null, FlowableConst.TASK_TIP_TYPE_UN_FINISH, flowableMsgCreate);
                }
            }
            return "操作成功";
        }catch (Exception e){
            String msg=StrUtil.format("重发审批流程消息提醒失败->{}", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }
    }

    @Override
    public FlowableModelVo fillModel(TcFlowableModel flowableModel) {
        ValidationUtils.validateEntity(flowableModel);
        String processDefinitionKey = flowableModel.getProcessDefinitionKey();
        LambdaQueryWrapper<TcFlowableElements> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcFlowableElements::getProcessDefinitionKey,processDefinitionKey);
        List<TcFlowableElements> flowableElementsList=flowableElementsMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtil.isEmpty(flowableElementsList)){
            throw new CommonException("流程节点数据为空");
        }
        FlowableModelVo flowableModelVo=new FlowableModelVo();
        flowableModelVo.setProcessDefinitionKey(flowableModel.getProcessDefinitionKey());
        flowableModelVo.setProcessDefinitionName(flowableModel.getProcessDefinitionName());

        List<EdgeElement> edgeElements = FlowableUtil.convertFlowableElementToEdgeElement(flowableElementsList);
        flowableModelVo.setEdges(edgeElements);
        List<NodeElement> nodeElements = FlowableUtil.convertFlowableElementToNodeElement(flowableElementsList);
        flowableModelVo.setNodes(nodeElements);

        return flowableModelVo;
    }

    @Override
    public TcFlowableModelMapper getMapper() {
        return flowableModelMapper;
    }

    @Override
    public List<TcFlowableModel> selectByList(QueryData<TcFlowableModel> queryData) {
        return FlowableModelService.super.selectByList(queryData);
    }

    @Override
    public Page<TcFlowableModel> selectByPage(QueryData<TcFlowableModel> queryData) {
        return FlowableModelService.super.selectByPage(queryData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteData(List<TcFlowableModel> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空，无法删除");
        }
        List<TcFlowableModel> deleteList = list.stream().filter(item -> StrUtil.isNotBlank(item.getProcessDefinitionKey())).collect(Collectors.toList());
        FlowableService flowableService = SpringBeanUtil.getBeanByClass(FlowableService.class);
        FlowableModelService flowableModelService = SpringBeanUtil.getBeanByClass(FlowableModelService.class);
        //校验是否存在流程实例
        if (CollectionUtil.isNotEmpty(deleteList)) {
            for (TcFlowableModel model : deleteList) {
                if (flowableService.existDeploymentByProcessDefinitionKey(model.getProcessDefinitionKey())) {
                    if (flowableService.existProcessInstanceByProcessDefinitionKey(model.getProcessDefinitionKey())) {
                        String msg = StrUtil.format("流程模型key->{},存在流程实例，无法删除", model.getProcessDefinitionKey());
                        throw new CommonException(msg);
                    }
                }
            }
        }
        return flowableModelService.forceDeleteData(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String forceDeleteData(List<TcFlowableModel> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空，无法删除");
        }
        List<TcFlowableModel> deleteList = list.stream().filter(item -> StrUtil.isNotBlank(item.getProcessDefinitionKey())).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(deleteList)){
            FlowableService flowableService = SpringBeanUtil.getBeanByClass(FlowableService.class);
            //删除实际流程
            for (TcFlowableModel model:deleteList){
                flowableService.deleteDeploymentByKey(model.getProcessDefinitionKey());
            }

            //删除模型数据
            List<String> processDefinitionKeyList = deleteList.stream().map(TcFlowableModel::getProcessDefinitionKey).collect(Collectors.toList());
            LambdaQueryWrapper<TcFlowableModel> flowableModelLambdaQueryWrapper=new LambdaQueryWrapper<>();
            flowableModelLambdaQueryWrapper.in(TcFlowableModel::getProcessDefinitionKey,processDefinitionKeyList);
            flowableModelMapper.delete(flowableModelLambdaQueryWrapper);

            LambdaQueryWrapper<TcFlowableElements> flowableElementsLambdaQueryWrapper=new LambdaQueryWrapper<>();
            flowableElementsLambdaQueryWrapper.in(TcFlowableElements::getProcessDefinitionKey,processDefinitionKeyList);
            flowableElementsMapper.delete(flowableElementsLambdaQueryWrapper);
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateData(List<TcFlowableModel> list) {
        return null;
    }

    @Override
    public Map<String,Object> getDictList() {
        Map<String,Object> result=new HashMap<>();

        List<TcUser> userList = userService.getUserList();
        Map<String, String> userIdAndNameMapping= userList.stream().collect(Collectors.toMap(TcUser::getId,TcUser::getNickName));
        result.put("userIdMapping",userIdAndNameMapping);

        return result;
    }

    @Override
    public Page<ProcessInstanceVo> getProcInsPageByProcDefKey(ProcessInstancePageQuery query) {
        TcFlowableModel model = query.getModel();
        long size = query.getSize();
        long current = query.getCurrent();
        long offset = SqlGenerateUtil.getOffset(current, size);
        String businessKey = query.getBusinessKey();
        String name = query.getName();


        List<ProcessInstanceVo> processInstanceVos=new ArrayList<>();
        long total;
        if (!query.isHis()){
            total = runtimeService
                    .createProcessInstanceQuery()
                    .active()
                    .processDefinitionKey(model.getProcessDefinitionKey())
                    .count();
            ProcessInstanceQuery processInstanceQuery =
                    runtimeService
                            .createProcessInstanceQuery()
                            .active()
                            .processDefinitionKey(model.getProcessDefinitionKey());
            if (StrUtil.isNotBlank(businessKey)){
                processInstanceQuery.processInstanceBusinessKeyLike("%"+businessKey+"%");
            }
            if (StrUtil.isNotBlank(name)){
                processInstanceQuery.processInstanceNameLike("%"+name+"%");
            }
            List<ProcessInstance> processInstanceList = processInstanceQuery
                    .listPage((int) offset, (int) size);
            if (CollectionUtil.isNotEmpty(processInstanceList)){
                for (ProcessInstance processInstance:processInstanceList){
                    processInstanceVos.add(FlowableUtil.procInsConvertProcInsVo(processInstance));
                }
            }
        }else {
            total = historyService.
                    createHistoricProcessInstanceQuery()
                    .finished()
                    .processDefinitionKey(model.getProcessDefinitionKey())
                    .count();
            HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService
                    .createHistoricProcessInstanceQuery()
                    .finished()
                    .processDefinitionKey(model.getProcessDefinitionKey());
            if (StrUtil.isNotBlank(businessKey)){
                historicProcessInstanceQuery.processInstanceBusinessKeyLike("%"+businessKey+"%");
            }
            if (StrUtil.isNotBlank(name)){
                historicProcessInstanceQuery.processInstanceNameLike("%"+name+"%");
            }
            List<HistoricProcessInstance> historicProcessInstanceList = historicProcessInstanceQuery.listPage((int) offset, (int) size);
            if (CollectionUtil.isNotEmpty(historicProcessInstanceList)){
                for (HistoricProcessInstance historicTaskInstance:historicProcessInstanceList){
                    processInstanceVos.add(FlowableUtil.hisProcInsConvertProcInsVo(historicTaskInstance));
                }
            }
        }
        Page<ProcessInstanceVo> page=new Page<>(current,size);
        page.setRecords(processInstanceVos);
        page.setTotal(total);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteProcessInstance(List<ProcessInstanceVo> processInstanceVoList) {
        if (CollectionUtil.isEmpty(processInstanceVoList)){
            throw new CommonException("删除流程实例不能为空");
        }
        for (ProcessInstanceVo processInstanceVo:processInstanceVoList){
            if (processInstanceVo.isHis()){
                historyService.deleteHistoricProcessInstance(processInstanceVo.getId());
            }else {
                runtimeService.deleteProcessInstance(processInstanceVo.getId(),"手动删除");
            }
        }
        return "操作成功";
    }
}
