package com.xm.flowable.service.impl;


import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;

import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.flowable.cmd.RollBackResult;
import com.xm.flowable.domain.dto.*;
import com.xm.flowable.domain.res.ExecuteTaskRes;
import com.xm.flowable.domain.vo.ProcessInstanceVo;
import com.xm.flowable.domain.vo.TaskInfoVo;
import com.xm.flowable.enums.OtherVariableNameEnum;
import com.xm.flowable.enums.ProcessVariableNameEnum;
import com.xm.flowable.enums.TaskStatusEnum;
import com.xm.flowable.enums.TaskVariableNameEnum;
import com.xm.flowable.listener.FlowableTaskListener;
import com.xm.flowable.service.FlowableModelService;
import com.xm.flowable.service.FlowableService;
import com.xm.flowable.cmd.SimpleProcessRollbackCmd;
import com.xm.util.FlowableUtil;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.lock.LockUtil;
import com.xm.util.valid.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.common.engine.impl.el.VariableContainerWrapper;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlowableServiceImpl implements FlowableService {

    private final RepositoryService repositoryService;

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final HistoryService historyService;

    private final ExpressionManager expressionManager;

    private final ManagementService managementService;

    private List<UserTask> createUserTaskBySimpleFlowUserList(List<SimpleFlowUser> flowUserList){
        if (CollectionUtil.isEmpty(flowUserList)){
            throw new CommonException("数据为空");
        }
        List<UserTask> userTaskList=new ArrayList<>();
        for (SimpleFlowUser simpleFlowUser:flowUserList){
            ValidationUtils.validateEntity(simpleFlowUser);
            UserTask userTask=new UserTask();
            userTask.setId(FlowableUtil.generaId());
            userTask.setName(simpleFlowUser.getName());
            userTask.setAssignee(simpleFlowUser.getAssignee());

            userTaskList.add(userTask);
        }
        return userTaskList;
    }

    private void createSimpleFlow(Process mainProcess, List<SimpleFlowCreate> simpleFlow, FlowNode startFlowNode, FlowNode endFlowNode){
        FlowElement preFlowElement=startFlowNode;
        for (SimpleFlowCreate simpleFlowCreate : simpleFlow) {
            ValidationUtils.validateEntity(simpleFlowCreate);

            boolean parallel = simpleFlowCreate.isParallel();
            List<SimpleFlowUser> flowUserList = simpleFlowCreate.getFlowUserList();
            if (parallel && flowUserList.size() < 2) {
                throw new CommonException("并行流至少两个节点");
            }
            List<UserTask> userTaskList = createUserTaskBySimpleFlowUserList(simpleFlowCreate.getFlowUserList());
            if (parallel) {
                ParallelGateway parallelGatewayStart;
                if (preFlowElement instanceof Gateway) {
                    parallelGatewayStart = (ParallelGateway) preFlowElement;
                } else {
                    parallelGatewayStart = new ParallelGateway();
                    parallelGatewayStart.setId(FlowableUtil.generaId());
                    mainProcess.addFlowElement(parallelGatewayStart);
                    mainProcess.addFlowElement(new SequenceFlow(preFlowElement.getId(), parallelGatewayStart.getId()));
                }

                for (UserTask userTask : userTaskList) {
                    mainProcess.addFlowElement(userTask);
                    mainProcess.addFlowElement(new SequenceFlow(parallelGatewayStart.getId(), userTask.getId()));
                }

                ParallelGateway parallelGatewayEnd = new ParallelGateway();
                parallelGatewayEnd.setId(FlowableUtil.generaId());
                mainProcess.addFlowElement(parallelGatewayEnd);
                for (UserTask userTask : userTaskList) {
                    mainProcess.addFlowElement(new SequenceFlow(userTask.getId(), parallelGatewayEnd.getId()));
                }

                preFlowElement = parallelGatewayEnd;
            } else {
                if (userTaskList.size() != 1) {
                    throw new CommonException("非并行节点审批人数应等于1");
                }
                UserTask userTask = userTaskList.get(0);
                mainProcess.addFlowElement(userTask);
                mainProcess.addFlowElement(new SequenceFlow(preFlowElement.getId(), userTask.getId()));

                preFlowElement = userTask;
            }
        }
        mainProcess.addFlowElement(new SequenceFlow(preFlowElement.getId(),endFlowNode.getId()));
    }

    private BpmnModel creatSimpleBpmModel(List<List<SimpleFlowCreate>> simpleFlowCreateList,String processDefinitionKey) {
        if (CollectionUtil.isEmpty(simpleFlowCreateList)){
            throw new CommonException("数据为空，无法创建审批流模型");
        }
        BpmnModel bpmnModel=new BpmnModel();
        Process mainProcess = new Process();
        mainProcess.setId(processDefinitionKey);
        mainProcess.setName("流程");
        bpmnModel.addProcess(mainProcess);

        StartEvent startEvent=new StartEvent();
        startEvent.setId(FlowableUtil.generaId());
        startEvent.setName("开始");
        mainProcess.addFlowElement(startEvent);

        EndEvent endEvent=new EndEvent();
        endEvent.setId(FlowableUtil.generaId());
        endEvent.setName("结束");
        mainProcess.addFlowElement(endEvent);

        if (simpleFlowCreateList.size()==1){
            for (List<SimpleFlowCreate> simpleFlow:simpleFlowCreateList){
                createSimpleFlow(mainProcess,simpleFlow,startEvent,endEvent);
            }
        }else {
            ParallelGateway startGateway=new ParallelGateway();
            startGateway.setId(FlowableUtil.generaId());
            ParallelGateway endGateway=new ParallelGateway();
            endGateway.setId(FlowableUtil.generaId());

            mainProcess.addFlowElement(startGateway);
            mainProcess.addFlowElement(endGateway);
            mainProcess.addFlowElement(new SequenceFlow(startEvent.getId(),startGateway.getId()));
            mainProcess.addFlowElement(new SequenceFlow(endGateway.getId(),endEvent.getId()));

            for (List<SimpleFlowCreate> simpleFlow:simpleFlowCreateList){
                createSimpleFlow(mainProcess,simpleFlow,startGateway,endGateway);
            }
        }
        return bpmnModel;
    }


    @Override
    public void createSimpleDeployment(FlowableSimpleBpmnDeployment flowableSimpleBpmnDeployment) {
        ValidationUtils.validateEntity(flowableSimpleBpmnDeployment);
        List<List<SimpleFlowCreate>> simpleFlowCreateList = flowableSimpleBpmnDeployment.getSimpleFlowCreateList();
        String processDefinitionKey = flowableSimpleBpmnDeployment.getProcessDefinitionKey();
        String name = flowableSimpleBpmnDeployment.getName();
        boolean cover = flowableSimpleBpmnDeployment.isCover();
        String bpmName=StrUtil.format("{}.bpmn",processDefinitionKey);

        BpmnModel bpmnModel = creatSimpleBpmModel(simpleFlowCreateList,processDefinitionKey);

        Deployment database = repositoryService.createDeploymentQuery()
                .deploymentKey(processDefinitionKey).singleResult();
        if (database!=null){
            if (cover){
                deleteDeploymentById(database.getId());
            }else {
                String msg= StrUtil.format("key->{}流程已存在,请重命名",processDefinitionKey);
                throw new CommonException(msg);
            }
        }
//        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
//        byte[] convertToXML = bpmnXMLConverter.convertToXML(bpmnModel);

        //自动调整布局
        BpmnAutoLayout bpmnAutoLayout = new BpmnAutoLayout(bpmnModel);
        bpmnAutoLayout.execute();

        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment
                .addBpmnModel(bpmName,bpmnModel)
//                .addBytes(bpmName,convertToXML)
                .key(processDefinitionKey)
                .name(name)
                .deploy();

        //存储模型
        FlowableModelService beanByClass = SpringBeanUtil.getBeanByClass(FlowableModelService.class);
        beanByClass.recordModel(processDefinitionKey,name);

        log.info("模型部署,模型id->{},模型key->{},模型名称->{}",deploy.getId(),deploy.getKey(),deploy.getName());
    }

    @Override
    public boolean existDeploymentByProcessDefinitionKey(String processDefinitionKey) {
        long count = repositoryService.createDeploymentQuery().deploymentKey(processDefinitionKey).count();
        return count != 0;
    }

    @Override
    public boolean existProcessInstanceByProcessDefinitionKey(String processDefinitionKey) {
        long count = runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count();
        long hisCount = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count();
        long allCount=count+hisCount;
        return allCount != 0;
    }

    @Override
    public ProcessInstance startProcess(StartProcessInstance startProcessInstance){
        ValidationUtils.validateEntity(startProcessInstance);
        String processDefinitionKey = startProcessInstance.getProcessDefinitionKey();
        String id = startProcessInstance.getId();
        String name = startProcessInstance.getName();
        Class<? extends FlowableTaskListener> listener = startProcessInstance.getListener();
        Map<String, Object> data = startProcessInstance.getData();
        String description = startProcessInstance.getDescription();
        String jumpUrl = startProcessInstance.getJumpUrl();
        String title = startProcessInstance.getTitle();
        String businessKey = startProcessInstance.getBusinessKey();
        String businessNo = startProcessInstance.getBusinessNo();
        String businessType = startProcessInstance.getBusinessType();

        if (data==null){
            data=new HashMap<>();
        }
        if (listener!=null){
            data.put(ProcessVariableNameEnum.listener.name(),listener.getName());
        }
        if (StrUtil.isBlank(description)){
            description="";
        }
        if (StrUtil.isBlank(id)){
            id="";
        }
        data.put(ProcessVariableNameEnum.description.name(),description);
        data.put(ProcessVariableNameEnum.jumpUrl.name(), jumpUrl);
        data.put(ProcessVariableNameEnum.title.name(), title);
        data.put(ProcessVariableNameEnum.businessNo.name(), businessNo);
        data.put(ProcessVariableNameEnum.businessKey.name(),businessKey);
        data.put(ProcessVariableNameEnum.businessType.name(),businessType);
        data.put(ProcessVariableNameEnum.id.name(),id);

        ProcessInstance start = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey(processDefinitionKey)
                .name(name)
                .businessKey(businessKey).variables(data).start();
        ProcessInstanceVo processInstanceVo = FlowableUtil.procInsConvertProcInsVo(start);
        executeTaskCallBack(processInstanceVo,null,TaskStatusEnum.init,false,null);
        log.info("流程启动,模型id->{},模型key->{},流程id->{},流程名称->{},流程业务编码->{}",start.getDeploymentId(),start.getProcessDefinitionKey(),start.getId(),start.getName(),start.getBusinessKey());
        return start;
    }


    @Override
    public List<TaskInfoVo> processAllTask(String processInstanceId) {
        List<TaskInfoVo> taskInfoVos=new ArrayList<>();

        List<HistoricTaskInstance> hisList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished().taskWithoutDeleteReason()
                .orderByHistoricTaskInstanceEndTime().asc()
                .includeProcessVariables().includeTaskLocalVariables().list();

        if (CollectionUtil.isNotEmpty(hisList)){
            for (HistoricTaskInstance task:hisList){
                taskInfoVos.add(FlowableUtil.hisTaskConvertToTaskInfoVo(task));
            }
        }

        List<Task> list = taskService.createTaskQuery()
                .active()
                .processInstanceId(processInstanceId).includeProcessVariables().includeTaskLocalVariables().list();
        if (CollectionUtil.isNotEmpty(list)){
            for (Task task:list){
                taskInfoVos.add(FlowableUtil.taskConvertToTaskInfoVo(task));
            }
        }

        return taskInfoVos;
    }

    @Override
    public List<TaskInfoVo> processAllTaskWithoutData(String processInstanceId) {
        List<TaskInfoVo> taskInfoVos=new ArrayList<>();

        List<HistoricTaskInstance> hisList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished().taskWithoutDeleteReason().list();
        if (CollectionUtil.isNotEmpty(hisList)){
            for (HistoricTaskInstance task:hisList){
                taskInfoVos.add(FlowableUtil.hisTaskConvertToTaskInfoVo(task));
            }
        }

        List<Task> list = taskService.createTaskQuery()
                .active()
                .processInstanceId(processInstanceId).list();
        if (CollectionUtil.isNotEmpty(list)){
            for (Task task:list){
                taskInfoVos.add(FlowableUtil.taskConvertToTaskInfoVo(task));
            }
        }

        return taskInfoVos;
    }

    @Override
    public List<TaskInfoVo> processActiveTaskWithoutData(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        List<TaskInfoVo> taskInfoVos=new ArrayList<>();
        if (processInstance==null){
            return taskInfoVos;
        }
        if (processInstance.isSuspended()){
            return taskInfoVos;
        }
        List<Task> list = taskService.createTaskQuery()
                .active().processInstanceId(processInstanceId).list();
        if (CollectionUtil.isNotEmpty(list)){
            for (Task task:list){
                taskInfoVos.add(FlowableUtil.taskConvertToTaskInfoVo(task));
            }
        }
        return taskInfoVos;
    }

    @Override
    public long processActiveTaskCountWithoutData(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance==null){
            return 0;
        }
        if (processInstance.isSuspended()){
            return 0;
        }
        return taskService.createTaskQuery()
                .active().processInstanceId(processInstanceId).count();
    }

    @Override
    public List<TaskInfoVo> getTaskByUserAndProcessId(TcUser user, String processInstanceId) {
        if (user==null){
            throw new CommonException("用户为null，无法操作");
        }
        List<Task> list = taskService.createTaskQuery()
                .active()
                .taskAssignee(user.getId())
                .processInstanceId(processInstanceId).includeProcessVariables().includeTaskLocalVariables().list();
        List<TaskInfoVo> taskInfoVos=new ArrayList<>();
        if (CollectionUtil.isEmpty(list)){
            return taskInfoVos;
        }
        for (Task task:list){
            taskInfoVos.add(FlowableUtil.taskConvertToTaskInfoVo(task));
        }
        return taskInfoVos;
    }

    @Override
    public long getTaskCountByUserAndProcessId(TcUser user, String processInstanceId) {
        if (user==null){
            throw new CommonException("用户为null，无法操作");
        }
        return taskService.createTaskQuery()
                .active()
                .taskAssignee(user.getId())
                .processInstanceId(processInstanceId).count();
    }

    private void executeTaskCallBack(ProcessInstanceVo processInstance, TaskInfoVo taskInfoVo, TaskStatusEnum taskStatusEnum, boolean isFinish, Map<String,Object> otherVariable){
        Map<String, Object> processVariables = processInstance.getProcessVariables();
        String listenerClassName = (String) processVariables.get(ProcessVariableNameEnum.listener.name());
        if (StrUtil.isNotBlank(listenerClassName)){
            FlowableTaskListener beanByClass;
            try {
                beanByClass = (FlowableTaskListener) SpringBeanUtil.getBeanByClass(Class.forName(listenerClassName));
                beanByClass.execute(processInstance,taskInfoVo,taskStatusEnum,isFinish,otherVariable);
            } catch (ClassNotFoundException e) {
                log.info("流程id->{},业务监听类不存在->{}",processInstance.getId(),ExceptionUtil.stacktraceToString(e));
            }
        }
    }

    private void executeTaskWithOutLock(ExecuteTask executeTask){
        TcUser userBySession = executeTask.getUser();
        if (userBySession==null){
            throw new CommonException("未登录无法操作");
        }
        String taskId = executeTask.getTaskId();
        String status = executeTask.getStatus().name();
        String msg = executeTask.getMsg();
        if (StrUtil.isBlank(taskId)){
            throw new CommonException("任务id为空，无法执行");
        }
        Task task = taskService.createTaskQuery().taskId(taskId)
                .includeProcessVariables().includeTaskLocalVariables().singleResult();
        if (task==null){
            throw new CommonException("任务id不存在，无法执行");
        }
        if (StrUtil.isBlank(msg)){
            msg="";
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .includeProcessVariables().singleResult();
        if (processInstance==null){
            throw new CommonException("流程不存在，无法继续操作");
        }
        if (processInstance.isSuspended()){
            throw new CommonException("审批已驳回，无法继续操作");
        }

//        if (!StrUtil.equals(id,task.getAssignee())){
//            //领取任务
//            taskService.unclaim(task.getId());
//            taskService.claim(task.getId(),id);
//            throw new CommonException("执行人和当前用户不匹配");
//        }
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.mapping.get(status);
        if (taskStatusEnum==null){
            throw new CommonException("非法任务执行状态");
        }
        //设置审批状态和审批信息
        Map<String,Object> localVariable=new HashMap<>();
        localVariable.put(TaskVariableNameEnum.status.name(),status);
        localVariable.put(TaskVariableNameEnum.msg.name(), msg);
        String actionTime = DateUtil.formatDateTime(new Date());
        localVariable.put(TaskVariableNameEnum.actionTime.name(), actionTime);

        if (taskStatusEnum != TaskStatusEnum.transfer){
            taskService.setVariablesLocal(task.getId(),localVariable);
        }

        //转换task
        TaskInfoVo taskInfoVo = FlowableUtil.taskConvertToTaskInfoVo(task);
        taskInfoVo.setTaskLocalVariables(localVariable);
        taskInfoVo.setMsg(msg);
        taskInfoVo.setActionTime(actionTime);
        taskInfoVo.setStatus(TaskVariableNameEnum.status.name());

        Map<String,Object> otherVariable=new HashMap<>();
        otherVariable.put(OtherVariableNameEnum.executeTask.name(),executeTask);

        boolean isFinish = false;
        ProcessInstanceVo processInstanceVo = FlowableUtil.procInsConvertProcInsVo(processInstance);
        if (taskStatusEnum == TaskStatusEnum.reject||taskStatusEnum == TaskStatusEnum.suspend){
            //流程执行
            taskService.complete(task.getId());
            //停止流程
            suspendProcess(task.getProcessInstanceId());
            isFinish=true;
            executeTaskCallBack(processInstanceVo,taskInfoVo,taskStatusEnum,isFinish,otherVariable);
        }else if (taskStatusEnum == TaskStatusEnum.success){
            //流程执行
            taskService.complete(task.getId());
            //查询当前是否还存在任务
            long count= processActiveTaskCountWithoutData(processInstance.getId());
            if (count==0){
                isFinish=true;
            }
            executeTaskCallBack(processInstanceVo,taskInfoVo,taskStatusEnum,isFinish,otherVariable);
        }else if (taskStatusEnum == TaskStatusEnum.rollback){
            //回滚
            RollBackResult rollBackResult = rollbackSimpleProcess(processInstance,task.getExecutionId());
            if (rollBackResult.isFinish()){
                isFinish=true;
            }
            executeTaskCallBack(processInstanceVo,taskInfoVo,taskStatusEnum,isFinish,otherVariable);
        }else if (taskStatusEnum == TaskStatusEnum.transfer){
            if (taskInfoVo.getAssignee().equals(executeTask.getMsg())){
                throw new CommonException("相同人员无法转办");
            }
            taskService.unclaim(task.getId());
            taskService.claim(task.getId(),executeTask.getMsg());
            executeTaskCallBack(processInstanceVo,taskInfoVo,taskStatusEnum,isFinish,otherVariable);
        } else if (taskStatusEnum == TaskStatusEnum.delete){
            isFinish=true;
            //删除流程
            FlowableService beanByClass = SpringBeanUtil.getBeanByClass(FlowableService.class);
            beanByClass.deleteProcess(task.getProcessInstanceId());
        }

        ExecuteTaskRes executeTaskRes=new ExecuteTaskRes();
        executeTaskRes.setTaskInfoVo(taskInfoVo);
        executeTaskRes.setFinish(isFinish);
        executeTaskRes.setTaskStatusEnum(taskStatusEnum);
        executeTaskRes.setProcessInstance(processInstance);
    }

    @Override
    public void executeTask(ExecuteTask executeTask){
        LockUtil.lock(executeTask.getTaskId(), ()->{
            executeTaskWithOutLock(executeTask);
            return "";
        },(e)->{
            String msg=StrUtil.format("审批失败->{}", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        });
    }

    @Override
    public boolean activityProcess(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance.isEnded()){
            return true;
        }
        if (processInstance.isSuspended()){
            runtimeService.activateProcessInstanceById(processInstanceId);
        }
        return true;
    }

    @Override
    public void suspendProcess(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance==null){
            return;
        }
        if (processInstance.isEnded()){
            return;
        }
        if (!processInstance.isSuspended()){
            runtimeService.suspendProcessInstanceById(processInstanceId);
        }
        runtimeService.deleteProcessInstance(processInstanceId,"执行拒绝,中止流程");
    }

    @Override
    public void deleteSuspendProcess(String processInstanceId) {
        long count = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).count();
        if (count!=0){
            historyService.deleteHistoricProcessInstance(processInstanceId);
        }
    }

    @Override
    public void deleteProcess(String processInstanceId) {
        //现在
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .includeProcessVariables()
                .singleResult();
        //历史
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).includeProcessVariables().singleResult();
        if (processInstance!=null&&historicProcessInstance!=null){
            //先执行回调方法再删除流程
            ProcessInstanceVo processInstanceVo = FlowableUtil.procInsConvertProcInsVo(processInstance);
            executeTaskCallBack(processInstanceVo,null,TaskStatusEnum.delete,true,null);
            runtimeService.deleteProcessInstance(processInstanceId,"删除");
            historyService.deleteHistoricProcessInstance(processInstanceId);
        }
        if (processInstance!=null&&historicProcessInstance==null){
            //先执行回调方法再删除流程
            ProcessInstanceVo processInstanceVo = FlowableUtil.procInsConvertProcInsVo(processInstance);
            executeTaskCallBack(processInstanceVo,null,TaskStatusEnum.delete,true,null);
            runtimeService.deleteProcessInstance(processInstanceId,"删除");
        }
        if (processInstance==null&&historicProcessInstance!=null){
            //先执行回调方法再删除流程
            ProcessInstanceVo processInstanceVo = FlowableUtil.hisProcInsConvertProcInsVo(historicProcessInstance);
            executeTaskCallBack(processInstanceVo,null,TaskStatusEnum.delete,true,null);
            historyService.deleteHistoricProcessInstance(processInstanceId);
        }
    }

    @Override
    public void deleteDeploymentById(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId,true);
    }

    @Override
    public void deleteDeploymentByKey(String key) {
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentKey(key).singleResult();
        if (deployment==null){
            return;
        }
        deleteDeploymentById(deployment.getId());
    }


    @Override
    public String viewProcessImageBase64(String processInstanceId) {
        if (StrUtil.isBlank(processInstanceId)){
            throw new CommonException("流程id为空");
        }
        InputStream processImage = getProcessImage(processInstanceId);
        if (processImage==null){
            return null;
        }
        return Base64.encode(processImage);
    }

    @Override
    public Object getExpressionValue(Map<String, Object> variables, String expression) {
        try {
            Expression expressionBean = expressionManager.createExpression(expression);
            VariableContainerWrapper variableContainerWrapper=new VariableContainerWrapper(variables);
//            DelegateExecution delegateExecution = new ExecutionEntityImpl();
//            delegateExecution.setVariables(variables);
//            delegateExecution.setTransientVariablesLocal(variables);
//            delegateExecution.setTransientVariables(variables);
            return expressionBean.getValue(variableContainerWrapper);
        }catch (Exception e){
            String msg=StrUtil.format("公式->{},参数->{}解析失败",expression,variables);
            log.error(msg,e);
            throw new CommonException("公式解析失败");
        }
    }

    @Override
    public void transfer(String taskId, String assignee) {
        taskService.unclaim(taskId);
        taskService.claim(taskId, assignee);
    }

    private RollBackResult rollbackSimpleProcess(ProcessInstance processInstance,String executionId){
        RollBackResult rollBackResult = managementService.executeCommand(new SimpleProcessRollbackCmd(processInstance, executionId));
        if (rollBackResult.isFinish()){
            runtimeService.deleteProcessInstance(processInstance.getId(),"回滚");
            historyService.deleteHistoricProcessInstance(processInstance.getId());
        }else {
            //处理转办问题
            Map<String, String> upstreamActIdAndAssigneeMapping = rollBackResult.getUpstreamActIdAndAssigneeMapping();
            if (CollectionUtil.isNotEmpty(upstreamActIdAndAssigneeMapping)){
                List<Task> activeList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list();
                for (Task task:activeList){
                    String assignee = upstreamActIdAndAssigneeMapping.get(task.getTaskDefinitionKey());
                    if (StrUtil.isNotBlank(assignee)){
                        taskService.setAssignee(task.getId(),assignee);
                    }
                }
            }
        }
        return rollBackResult;
    }

    @Override
    public boolean viewProcessImageByProcessId(String processInstanceId, HttpServletResponse response) {
        InputStream processImage = getProcessImage(processInstanceId);
        byte[] readBytes= IoUtil.readBytes(processImage);

        try {
            // 清空response
            response.reset();
            // 设置response的Header
            response.setCharacterEncoding("UTF-8");
            // 设置文件长度
            response.setContentLengthLong(readBytes.length);
            // 在线打开方式 文件名应该编码成UTF-8
            response.setHeader("Content-Disposition", "inline; filename=" + URLEncoder.encode("image.png", "UTF-8"));
            OutputStream outputStream=response.getOutputStream();
            outputStream.write(readBytes);
            outputStream.flush();
            outputStream.close();
            return true;
        }catch (Exception e){
            log.error("流程图查看失败",e);
            throw new CommonException("流程图查看失败");
        }
    }

    /**
     * 获取流程图
     */
    private InputStream getProcessImage(String processInstanceId) {
        String procDefId;
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .includeProcessVariables()
                .singleResult();
        Map<String,Object> variables;
        if (processInstance == null) {
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .includeProcessVariables()
                    .singleResult();
            if (historicProcessInstance==null){
                return null;
            }
            procDefId = historicProcessInstance.getProcessDefinitionId();
            variables=historicProcessInstance.getProcessVariables();
        } else {
            procDefId = processInstance.getProcessDefinitionId();
            variables=processInstance.getProcessVariables();
        }

        BpmnModel bpmnModel = repositoryService.getBpmnModel(procDefId);
        DefaultProcessDiagramGenerator defaultProcessDiagramGenerator = new DefaultProcessDiagramGenerator(); // 创建默认的流程图生成器
        String imageType = "png"; // 生成图片的类型
        List<String> highLightedFlows = new ArrayList<>(); // 高亮连线集合
        List<String> highLightedActivities=new ArrayList<>(); //高亮节点

//        // 查找所有历史活动
//        List<HistoricActivityInstance> hisActInsList = historyService.createHistoricActivityInstanceQuery()
//                .processInstanceId(processInstanceId).finished()
//                .list(); // 查询所有历史节点信息
//        for (HistoricActivityInstance hai:hisActInsList){
//            if("sequenceFlow".equals(hai.getActivityType())) {
//                // 添加高亮连线
//                highLightedFlows.add(hai.getActivityId());
//            } else {
//                // 添加高亮节点
//                highLightedActivities.add(hai.getActivityId());
//            }
//        }

//        // 查找所有当前活动
//        List<ActivityInstance> list = runtimeService.createActivityInstanceQuery().processInstanceId(processInstanceId).unfinished().list();
//        if (CollectionUtil.isNotEmpty(list)){
//            for (ActivityInstance ai : list) {
//                if (StrUtil.isBlank(ai.getDeleteReason())){
//                    if (ai.getActivityType().equals("sequenceFlow")){
//                        // 添加高亮连线
//                        highLightedFlows.add(ai.getActivityId());
//                    }else{
//                        // 添加高亮节点
//                        highLightedActivities.add(ai.getActivityId());
//                    }
//                }
//            }
//        }

        // 查找所有当前活动
        List<Task> list = taskService.createTaskQuery().active().processInstanceId(processInstanceId).list();
        if (CollectionUtil.isNotEmpty(list)){
            for (Task task:list){
                List<Execution> executionList = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).list();
                if (CollectionUtil.isNotEmpty(executionList)){
                    for (Execution execution:executionList){
                        highLightedActivities.add(execution.getActivityId());
                    }
                }
            }
        }

//        List<Execution> list = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).onlyChildExecutions().list();
//        if (CollectionUtil.isNotEmpty(list)){
//            for (Execution execution:list){
//                ExecutionEntityImpl entity= (ExecutionEntityImpl) execution;
//                if (!entity.isEnded()&&entity.isActive()&&!entity.isDeleted()){
//                    highLightedActivities.add(entity.getActivityId());
//                }
//            }
//        }


//        List<Task> list = taskService.createTaskQuery().active().processInstanceId(processInstanceId).list();
//        //        System.out.println(collect);
//        // 高亮节点集合
//        for (Task task:list){
//            Execution execution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
//            if (execution!=null){
//                highLightedActivities.add(execution.getActivityId());
//            }
//        }


        Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();

        for (FlowElement flowElement:flowElements){
            if (flowElement instanceof UserTask){
                UserTask userTask= (UserTask) flowElement;
                String name = userTask.getName();
                String assignee = userTask.getAssignee();
                if (StrUtil.isNotBlank(name)&&name.startsWith("${")){
                    userTask.setName(getExpressionValue(variables,name).toString());
                }
                if (StrUtil.isNotBlank(assignee)&&assignee.startsWith("${")){
                    userTask.setAssignee(getExpressionValue(variables,assignee).toString());
                }
            }
        }

        String activityFontName = "宋体"; // 节点字体
        String labelFontName = "微软雅黑"; // 连线标签字体
        String annotationFontName = "宋体"; // 连线标签字体
//        ClassLoader customClassLoader = null; // 类加载器
        double scaleFactor = 2.0d; // 比例因子，默认即可
        boolean drawSequenceFlowNameWithNoLabelDI = true; // 连线标签
        // 生成图片
        return defaultProcessDiagramGenerator.generateDiagram(bpmnModel, imageType, highLightedActivities
                , highLightedFlows, activityFontName, labelFontName, annotationFontName, null,
                scaleFactor, drawSequenceFlowNameWithNoLabelDI);
    }
}
