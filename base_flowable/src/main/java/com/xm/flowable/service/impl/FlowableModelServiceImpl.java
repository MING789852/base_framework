package com.xm.flowable.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.service.TcUserService;
import com.xm.module.core.params.QueryData;
import com.xm.flowable.domain.entity.TcFlowableElements;
import com.xm.flowable.domain.entity.TcFlowableModel;
import com.xm.flowable.domain.query.ProcessInstancePageQuery;
import com.xm.flowable.domain.vo.*;
import com.xm.flowable.enums.FlowableElementTypeEnum;
import com.xm.flowable.mapper.TcFlowableElementsMapper;
import com.xm.flowable.mapper.TcFlowableModelMapper;
import com.xm.flowable.service.FlowableModelService;
import com.xm.flowable.service.FlowableService;
import com.xm.util.FlowableUtil;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.sql.SqlGenerateUtil;
import com.xm.util.valid.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class FlowableModelServiceImpl implements FlowableModelService {
    private final TcFlowableElementsMapper flowableElementsMapper;
    private final TcFlowableModelMapper flowableModelMapper;
    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final TcUserService userService;
    private final HistoryService historyService;

    private void createBpmnModel(FlowableModelVo flowableModelVo){
        List<EdgeElement> edges = flowableModelVo.getEdges();
        List<NodeElement> nodes = flowableModelVo.getNodes();

        BpmnModel bpmnModel=new BpmnModel();
        Process mainProcess = new Process();
        mainProcess.setId(flowableModelVo.getProcessDefinitionKey());
        mainProcess.setName(flowableModelVo.getProcessDefinitionName());
        bpmnModel.addProcess(mainProcess);

        for (EdgeElement edgeElement:edges){
            if (FlowableElementTypeEnum.sequenceFlow.equals(edgeElement.getType())){
                SequenceFlow sequenceFlow = new SequenceFlow(edgeElement.getSourceNodeId(), edgeElement.getTargetNodeId());
                NodeText text = edgeElement.getText();
                if (text!=null&&StrUtil.isNotBlank(text.getValue())){
                    sequenceFlow.setName(text.getValue());
                }
                mainProcess.addFlowElement(new SequenceFlow(edgeElement.getSourceNodeId(),edgeElement.getTargetNodeId()));
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
                if (outEdgeElements.size()>1){
                    throw new CommonException("开始节点出度不能大于1，若要大于1，请中间衔接并行网关");
                }
                StartEvent startEvent = new StartEvent();
                startEvent.setId(nodeElement.getId());
                mainProcess.addFlowElement(startEvent);
                startEventNum++;
            }
            if (FlowableElementTypeEnum.parallelGateway.equals(nodeElement.getType())){
                if (CollectionUtil.isEmpty(outEdgeElements)){
                    throw new CommonException("网关节点出度不能为0");
                }
                if (CollectionUtil.isEmpty(inEdgeElements)){
                    throw new CommonException("网关节点入度不能为0");
                }
                ParallelGateway parallelGateway=new ParallelGateway();
                parallelGateway.setId(nodeElement.getId());
                mainProcess.addFlowElement(parallelGateway);
            }
            if (FlowableElementTypeEnum.userTask.equals(nodeElement.getType())){
                if (CollectionUtil.isEmpty(outEdgeElements)){
                    throw new CommonException("用户节点出度不能为0");
                }
                if (CollectionUtil.isEmpty(inEdgeElements)){
                    throw new CommonException("用户节点入度不能为0");
                }
                if (outEdgeElements.size()>1){
                    throw new CommonException("用户节点出度不能大于1，若要大于1，请中间衔接并行网关");
                }
                if (inEdgeElements.size()>1){
                    throw new CommonException("用户节点入度不能大于1，若要大于1，请中间衔接并行网关");
                }

                UserTask userTask=new UserTask();
                userTask.setId(nodeElement.getId());
                NodeProp properties = nodeElement.getProperties();
                if (properties==null){
                    throw new CommonException("用户节点properties为空");
                }
                String assignee = properties.getAssignee();
                if (StrUtil.isBlank(assignee)){
                    throw new CommonException("用户节点审批人不能为空");
                }
                NodeText text = nodeElement.getText();
                if (text==null){
                    throw new CommonException("用户节点名称不能为空");
                }
                if (StrUtil.isBlank(text.getValue())){
                    throw new CommonException("用户节点名称不能为空");
                }

                if (StrUtil.isNotBlank(properties.getCategory())){
                    userTask.setCategory(properties.getCategory());
                }
                userTask.setAssignee(assignee);
                userTask.setName(text.getValue());
                mainProcess.addFlowElement(userTask);
            }
            if (FlowableElementTypeEnum.endEvent.equals(nodeElement.getType())){
                if (CollectionUtil.isNotEmpty(outEdgeElements)){
                    throw new CommonException("结束节点出度必须为0");
                }
                if (CollectionUtil.isEmpty(inEdgeElements)){
                    throw new CommonException("结束节点入度不能为0");
                }
                if (inEdgeElements.size()>1){
                    throw new CommonException("结束节点入度不能大于1，若要大于1，请中间衔接并行网关");
                }
                EndEvent endEvent=new EndEvent();
                endEvent.setId(nodeElement.getId());
                mainProcess.addFlowElement(endEvent);
                endEventNum++;
            }
        }
        if (startEventNum!=1){
            throw new CommonException("开始节点只允许存在一个");
        }
        if (endEventNum!=1){
            throw new CommonException("结束节点只允许存在一个");
        }

        Deployment database = repositoryService.createDeploymentQuery()
                .deploymentKey(flowableModelVo.getProcessDefinitionKey()).singleResult();
        if (database!=null){
            String msg= StrUtil.format("流程定义KEY->{}流程已存在,请重命名",flowableModelVo.getProcessDefinitionKey());
            throw new CommonException(msg);
        }
        String bpmName=StrUtil.format("{}.bpmn",flowableModelVo.getProcessDefinitionKey());

        //自动调整布局
        new BpmnAutoLayout(bpmnModel).execute();

        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment
                .addBpmnModel(bpmName,bpmnModel)
                .key(flowableModelVo.getProcessDefinitionKey())
                .name(flowableModelVo.getProcessDefinitionName())
                .deploy();
        log.info("模型部署,模型id->{},模型key->{},模型名称->{}",deploy.getId(),deploy.getKey(),deploy.getName());
    }

    private List<TcFlowableElements> convertEdgeElementToFlowableElement(List<EdgeElement> edges,String processDefinitionKey,String processDefinitionName){
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
            elements.setProcessDefinitionKey(processDefinitionKey);
            elements.setProcessDefinitionName(processDefinitionName);

            flowableElements.add(elements);
        }
        return flowableElements;
    }

    private List<TcFlowableElements> convertNodeElementToFlowableElement(List<NodeElement> nodes,String processDefinitionKey,String processDefinitionName){
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
            NodeProp properties = nodeElement.getProperties();
            if (properties!=null){
                elements.setAssignee(properties.getAssignee());
                elements.setAssigneeName(properties.getAssigneeName());
                elements.setContent1(properties.getCategory());
            }
            elements.setProcessDefinitionKey(processDefinitionKey);
            elements.setProcessDefinitionName(processDefinitionName);

            flowableElements.add(elements);
        }
        return flowableElements;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String initUnRecordModel() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
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

            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).singleResult();
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
            //创建流程模型
            createBpmnModel(flowableModelVo);

            //存储流程节点数据
            List<TcFlowableElements> edgeFlowableElement = convertEdgeElementToFlowableElement(edges,
                    flowableModelVo.getProcessDefinitionKey(),flowableModelVo.getProcessDefinitionName());
            List<TcFlowableElements> nodeFlowableElement = convertNodeElementToFlowableElement(nodes,
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
                //创建流程模型
                createBpmnModel(flowableModelVo);
                //更新流程节点数据(先删除后新增)
                List<TcFlowableElements> edgeFlowableElement = convertEdgeElementToFlowableElement(edges,
                        flowableModelVo.getProcessDefinitionKey(),flowableModelVo.getProcessDefinitionName());
                List<TcFlowableElements> nodeFlowableElement = convertNodeElementToFlowableElement(nodes,
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
            }
        }

        return "操作成功";
    }

    private List<EdgeElement> convertFlowableElementToEdgeElement(List<TcFlowableElements> flowableElementsList){
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
                edgeElement.setProperties(new NodeProp());
                if (StrUtil.isNotBlank(flowableElements.getElementName())){
                    NodeText nodeText=new NodeText();
                    nodeText.setValue(flowableElements.getElementName());
                    nodeText.setX(flowableElements.getTextX());
                    nodeText.setY(flowableElements.getTextY());
                    edgeElement.setText(nodeText);
                }
                edgeElementList.add(edgeElement);
            }
        }
        return edgeElementList;
    }

    private List<NodeElement> convertFlowableElementToNodeElement(List<TcFlowableElements> flowableElementsList){
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
                NodeProp nodeProp=new NodeProp();
                nodeProp.setAssignee(flowableElements.getAssignee());
                nodeProp.setAssigneeName(flowableElements.getAssigneeName());
                nodeProp.setCategory(flowableElements.getContent1());
                nodeElement.setProperties(nodeProp);

                nodeElementList.add(nodeElement);
            }
        }
        return nodeElementList;
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

        List<EdgeElement> edgeElements = convertFlowableElementToEdgeElement(flowableElementsList);
        flowableModelVo.setEdges(edgeElements);
        List<NodeElement> nodeElements = convertFlowableElementToNodeElement(flowableElementsList);
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
            total = runtimeService.createProcessInstanceQuery().processDefinitionKey(model.getProcessDefinitionKey()).count();
            ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery()
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
            total = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(model.getProcessDefinitionKey()).count();
            HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
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
