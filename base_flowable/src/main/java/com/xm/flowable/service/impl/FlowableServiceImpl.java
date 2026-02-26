package com.xm.flowable.service.impl;


import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.service.TcUserService;
import com.xm.flowable.cmd.RollBackResult;
import com.xm.flowable.cmd.SimpleProcessRollbackCmd;
import com.xm.flowable.cmd.SpecifyProcessRollbackCmd;
import com.xm.flowable.domain.dto.*;
import com.xm.flowable.domain.params.CallBackOtherParams;
import com.xm.flowable.domain.res.ExecuteTaskRes;
import com.xm.flowable.domain.res.FlowableMsgCreate;
import com.xm.flowable.domain.vo.*;
import com.xm.flowable.enums.*;
import com.xm.flowable.listener.FlowableTaskListener;
import com.xm.flowable.listener.FlowableTestTipListener;
import com.xm.flowable.service.FlowableModelService;
import com.xm.flowable.service.FlowableService;
import com.xm.util.FlowableMsgUtil;
import com.xm.util.FlowableUtil;
import com.xm.util.auth.UserInfoUtil;
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

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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

    private final DynamicBpmnService dynamicBpmnService;

    private final TcUserService userService;

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
    public ProcessInstanceVo getProcessInstanceVoById(String processInstanceId) {
        if (StrUtil.isBlank(processInstanceId)){
            return null;
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .includeProcessVariables()
                .singleResult();
        if (processInstance==null){
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .includeProcessVariables()
                    .singleResult();
            if (historicProcessInstance==null){
                return null;
            }
            return FlowableUtil.hisProcInsConvertProcInsVo(historicProcessInstance);
        }else {
            return FlowableUtil.procInsConvertProcInsVo(processInstance);
        }
    }

    @Override
    public TaskInfoVo getTaskInfoVoById(String processInstanceId,String taskId) {
        if (StrUtil.isBlank(processInstanceId)||StrUtil.isBlank(taskId)){
            return null;
        }
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .processInstanceId(processInstanceId)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .singleResult();
        if (task==null){
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                    .includeProcessVariables()
                    .includeTaskLocalVariables()
                    .taskId(taskId)
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (historicTaskInstance==null){
                return null;
            }else {
                return FlowableUtil.hisTaskConvertToTaskInfoVo(historicTaskInstance);
            }
        }else {
            return FlowableUtil.taskConvertToTaskInfoVo(task);
        }
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
        TcUser creator = startProcessInstance.getCreator();

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

        data.put(ProcessVariableNameEnum.createTime.name(), DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
        if (creator!=null){
            data.put(ProcessVariableNameEnum.creator.name(),creator.getNickName());
            data.put(ProcessVariableNameEnum.creatorId.name(),creator.getId());
        }else {
            TcUser currentLoginUser = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
            if (currentLoginUser!=null){
                data.put(ProcessVariableNameEnum.creator.name(),currentLoginUser.getNickName());
                data.put(ProcessVariableNameEnum.creatorId.name(),currentLoginUser.getId());
            }
        }

        ProcessInstance start = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey(processDefinitionKey)
                .name(name)
                .businessKey(businessKey).variables(data).start();
        ProcessInstanceVo processInstanceVo = FlowableUtil.procInsConvertProcInsVo(start);
        executeTaskCallBack(processInstanceVo,null,TaskStatusEnum.init,false,null);
        log.info("流程启动,模型id->{},模型key->{},流程id->{},流程名称->{},流程业务编码->{}",start.getDeploymentId(),start.getProcessDefinitionKey(),start.getId(),start.getName(),start.getBusinessKey());


        //如果是启动审批,启动审批人员和当前审批人员相同，则直接通过
        if (startProcessInstance.isAutoExecuteTask()){
            List<TaskInfoVo> taskInfoVoList = FlowableUtil.currentUserValidateCanApprove(processInstanceVo.getId());
            if (CollectionUtil.isNotEmpty(taskInfoVoList)){
                ExecuteTask executeTask=new ExecuteTask(processInstanceVo.getId(),TaskStatusEnum.success,"系统自动通过");
                executeTask(executeTask);
                log.info("流程启动,流程id->{},流程名称->{},启动审批人员和当前审批人员相同,系统自动审批通过",start.getId(),start.getName());
            }
        }

        return start;
    }


    @Override
    public List<TaskInfoVo> processAllTask(String processInstanceId) {
        List<TaskInfoVo> taskInfoVos=new ArrayList<>();

        //查询已审批
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

        //查询正在审批
        List<Task> list = taskService.createTaskQuery()
                .active()
                .processInstanceId(processInstanceId).includeProcessVariables().includeTaskLocalVariables().list();
        if (CollectionUtil.isNotEmpty(list)){
            for (Task task:list){
                taskInfoVos.add(FlowableUtil.taskConvertToTaskInfoVo(task));
            }
        }

        //查询回滚
        ProcessInstanceVo processInstanceVo = getProcessInstanceVoById(processInstanceId);
        if (processInstanceVo!=null){
            Map<String,Object> variables=processInstanceVo.getProcessVariables();
            List<TaskInfoVo> rollBackRecordMsgList = Optional.ofNullable(variables.get(ProcessVariableNameEnum.rollBackRecordMsg.name()))
                    .map(rollBackRecordMsgObj -> JSONUtil.toList(rollBackRecordMsgObj.toString(), TaskInfoVo.class))
                    .orElse(new ArrayList<>());
            //处理排序
            taskInfoVos.addAll(rollBackRecordMsgList);
            taskInfoVos=taskInfoVos.stream().sorted(Comparator.comparing(TaskInfoVo::getCreateTime)).collect(Collectors.toList());

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

    private void executeTaskCallBack(ProcessInstanceVo processInstance, TaskInfoVo taskInfoVo, TaskStatusEnum taskStatusEnum, boolean isFinish, CallBackOtherParams params){
        Map<String, Object> processVariables = processInstance.getProcessVariables();
        String listenerClassName = FlowableUtil.getMapVariable(processVariables,ProcessVariableNameEnum.listener.name());
        if (StrUtil.isNotBlank(listenerClassName)){
            FlowableTaskListener beanByClass;
            try {
                String businessType = FlowableUtil.getMapVariable(processVariables,ProcessVariableNameEnum.businessType.name());
                String businessKey = FlowableUtil.getMapVariable(processVariables,ProcessVariableNameEnum.businessKey.name());
                if (businessKey==null){
                    throw new CommonException("业务key为空，处理审批回调失败");
                }
                if (businessType==null){
                    throw new CommonException("业务type为空，处理审批回调失败");
                }
                if(taskInfoVo==null){
                    taskInfoVo=new TaskInfoVo();
                }
                if (params==null){
                    params=new CallBackOtherParams();
                }
                //获取当前审批任务人
                List<TaskInfoVo> taskInfoVos = processActiveTaskWithoutData(processInstance.getId());
                List<String> currentApproveUserIdList = taskInfoVos.stream().map(TaskInfoVo::getAssignee).collect(Collectors.toList());

                beanByClass = (FlowableTaskListener) SpringBeanUtil.getBeanByClass(Class.forName(listenerClassName));
                beanByClass.execute(processInstance,currentApproveUserIdList,taskInfoVo,taskStatusEnum,isFinish,params);

                List<FlowableMsgCreate> flowableMsgCreates = beanByClass.getMsglist(processInstance, currentApproveUserIdList, taskInfoVo, taskStatusEnum, isFinish, params);
                FlowableMsgUtil.handleApprovingMsg(processInstance.getId(),processVariables, taskStatusEnum,currentApproveUserIdList,flowableMsgCreates, isFinish,
                        businessKey, businessType);
            } catch (ClassNotFoundException e) {
                log.info("流程id->{},业务监听类不存在->{}",processInstance.getId(),ExceptionUtil.stacktraceToString(e));
            }
        }
    }

    private void executeTaskWithOutLock(ProcessInstance processInstance, Task task,ExecuteTask executeTask){
        String status = executeTask.getStatus().name();
        String msg = executeTask.getMsg();
        TcUser executeTaskUser = executeTask.getUser();
        if (executeTaskUser==null){
            throw new CommonException("审批执行人员为空,无法操作");
        }
        Map<String, Object> variable = executeTask.getVariable();
        if (variable==null){
            variable=new HashMap<>();
        }
        if (StrUtil.isBlank(msg)){
            msg="";
        }
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.getEnumByName(status);
        if (taskStatusEnum==null){
            throw new CommonException("非法任务执行状态");
        }
        //设置审批状态和审批信息
        Map<String,Object> localVariable=new HashMap<>();
        localVariable.put(TaskVariableNameEnum.status.name(),status);
        localVariable.put(TaskVariableNameEnum.msg.name(), msg);
        localVariable.put(TaskVariableNameEnum.executeTaskUserName.name(), executeTaskUser.getNickName());
        localVariable.put(TaskVariableNameEnum.executeTaskUserId.name(), executeTaskUser.getId());
        String actionTime = DateUtil.formatDateTime(new Date());
        localVariable.put(TaskVariableNameEnum.actionTime.name(), actionTime);

        if (taskStatusEnum != TaskStatusEnum.transfer){
            if (CollectionUtil.isNotEmpty(localVariable)){
                for (Map.Entry<String,Object> entry:localVariable.entrySet()){
                    taskService.setVariableLocal(task.getId(),entry.getKey(),entry.getValue());
                }
            }
        }

        //转换task
        TaskInfoVo taskInfoVo = FlowableUtil.taskConvertToTaskInfoVo(task);
        taskInfoVo.setTaskLocalVariables(localVariable);
        taskInfoVo.setMsg(msg);
        taskInfoVo.setActionTime(actionTime);
        taskInfoVo.setStatus(TaskVariableNameEnum.status.name());

        boolean isFinish = false;
        ProcessInstanceVo processInstanceVo = FlowableUtil.procInsConvertProcInsVo(processInstance);
        CallBackOtherParams params=new CallBackOtherParams();
        if (taskStatusEnum == TaskStatusEnum.reject||taskStatusEnum == TaskStatusEnum.suspend){
            //流程执行
            taskService.complete(task.getId(),variable);
            //停止流程
            suspendProcess(task.getProcessInstanceId());
            isFinish=true;
            executeTaskCallBack(processInstanceVo,taskInfoVo,taskStatusEnum,isFinish,params);
        }else if (taskStatusEnum == TaskStatusEnum.success){
            //流程执行
            taskService.complete(task.getId(),variable);
            //查询当前是否还存在任务
            long count= processActiveTaskCountWithoutData(processInstance.getId());
            if (count==0){
                isFinish=true;
            }
            executeTaskCallBack(processInstanceVo,taskInfoVo,taskStatusEnum,isFinish,params);
        }else if (taskStatusEnum == TaskStatusEnum.rollback){
            List<String> rollbackTargetActIdList = executeTask.getRollbackTargetActIdList();
            if (CollectionUtil.isEmpty(rollbackTargetActIdList)){
                //回滚
                RollBackResult rollBackResult = rollbackSimpleProcess(processInstance,task.getExecutionId(),new RollBackWithData(false,task,""));
                isFinish=rollBackResult.isFinish();
            }else {
                RollBackResult rollBackResult = rollBackSpecifyProcess(processInstance,task.getExecutionId(),rollbackTargetActIdList,new RollBackWithData(false,task,""));
                isFinish=rollBackResult.isFinish();
            }
            executeTaskCallBack(processInstanceVo,taskInfoVo,taskStatusEnum,isFinish,params);
        }else if (taskStatusEnum == TaskStatusEnum.rollBackRecordMsg){
            List<String> rollbackTargetActIdList = executeTask.getRollbackTargetActIdList();
            if (CollectionUtil.isEmpty(rollbackTargetActIdList)){
                //回滚
                RollBackResult rollBackResult = rollbackSimpleProcess(processInstance,task.getExecutionId(),new RollBackWithData(true,task,msg));
                isFinish=rollBackResult.isFinish();
            }else {
                RollBackResult rollBackResult = rollBackSpecifyProcess(processInstance,task.getExecutionId(),rollbackTargetActIdList,new RollBackWithData(true,task,msg));
                isFinish=rollBackResult.isFinish();
            }
            executeTaskCallBack(processInstanceVo,taskInfoVo,taskStatusEnum,isFinish,params);
        } else if (taskStatusEnum == TaskStatusEnum.transfer){
            String transferUserId = executeTask.getTransferUserId();
            if (StrUtil.isBlank(transferUserId)){
                throw new CommonException("转办人员不能为空");
            }
            String userId = taskInfoVo.getAssignee();
            if (taskInfoVo.getAssignee().equals(transferUserId)){
                throw new CommonException("相同人员无法转办");
            }
            Map<String, Object> variables = taskInfoVo.getTaskLocalVariables();
            String transferUserListStr = FlowableUtil.getMapVariable(variables, TaskVariableNameEnum.transferUser.name());
            List<TransferUser> transferUserList=Optional.ofNullable(transferUserListStr)
                    .map(s -> JSONUtil.toList(s,TransferUser.class)).orElse(new ArrayList<>());
            transferUserList.add(new TransferUser(task.getTaskDefinitionKey(),userId,transferUserId));
            taskService.setVariableLocal(task.getId(),TaskVariableNameEnum.transferUser.name(),JSONUtil.toJsonStr(transferUserList));
            taskService.setAssignee(task.getId(),transferUserId);
//            taskService.setOwner(task.getId(),userId);
//            taskService.delegateTask(task.getId(),transferUserId);
//            taskService.resolveTask(task.getId());
            executeTaskCallBack(processInstanceVo,taskInfoVo,taskStatusEnum,isFinish,params);
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
        String processInstanceId = executeTask.getProcessInstanceId();
        TaskExecuteActionTypeEnum executeActionType = executeTask.getExecuteActionType();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .includeProcessVariables().singleResult();
        if (processInstance==null){
            throw new CommonException("流程不存在，无法继续操作");
        }
        if (processInstance.isSuspended()){
            throw new CommonException("审批已驳回，无法继续操作");
        }
        String taskId;
        if (TaskExecuteActionTypeEnum.customUser.equals(executeActionType)){
            TcUser userBySession = executeTask.getUser();
            if (userBySession==null){
                throw new CommonException("执行人员不能为空");
            }
            taskId = executeTask.getTaskId();
            if (StrUtil.isBlank(taskId)){
                throw new CommonException("任务id为空，无法执行");
            }
        }else {
            //自动校验是否有权限审批
            TcUser currentLoginUser = UserInfoUtil.getCurrentLoginUserBySessionOrTokenNotNull();
            List<TaskInfoVo> taskInfoVoList = FlowableUtil.currentUserValidateCanApprove(processInstanceId);
            if (CollectionUtil.isEmpty(taskInfoVoList)){
                throw new CommonException("无权限或无需执行审批");
            }
            TaskInfoVo taskInfoVo=taskInfoVoList.get(0);
            taskId = taskInfoVo.getId();
            executeTask.setUser(currentLoginUser);
            executeTask.setTaskId(taskId);
        }

        Task task = taskService.createTaskQuery().taskId(taskId)
                .includeProcessVariables().includeTaskLocalVariables().singleResult();
        if (task==null){
            throw new CommonException("任务id不存在，无法执行");
        }
        LockUtil.lock(processInstanceId, ()->{
            executeTaskWithOutLock(processInstance,task,executeTask);
            return "";
        },(e)->{
            log.error(StrUtil.format(StrUtil.format("审批失败->{}", ExceptionUtil.stacktraceToString(e))));
            throw new CommonException(StrUtil.format("审批失败->{}", e.getMessage()));
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
        List<Deployment> deploymentList = repositoryService.
                createDeploymentQuery().
                deploymentKey(key).list();
        if (CollectionUtil.isEmpty(deploymentList)){
            return;
        }
        for (Deployment deployment : deploymentList){
            deleteDeploymentById(deployment.getId());
        }
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

    private void handleRollBackResult(ProcessInstance processInstance,RollBackResult rollBackResult){
        if (rollBackResult.isFinish()){
            runtimeService.deleteProcessInstance(processInstance.getId(),"回滚");
            historyService.deleteHistoricProcessInstance(processInstance.getId());
        }
    }

    private RollBackResult rollBackSpecifyProcess(ProcessInstance processInstance, String executionId, List<String> rollbackTargetActIdList, RollBackWithData rollBackWithData){
        RollBackResult rollBackResult = managementService.executeCommand(new SpecifyProcessRollbackCmd(processInstance, executionId, rollbackTargetActIdList, rollBackWithData));
        handleRollBackResult(processInstance,rollBackResult);
        return rollBackResult;
    }

    private RollBackResult rollbackSimpleProcess(ProcessInstance processInstance,String executionId, RollBackWithData rollBackWithData){
        RollBackResult rollBackResult = managementService.executeCommand(new SimpleProcessRollbackCmd(processInstance, executionId,rollBackWithData));
        handleRollBackResult(processInstance,rollBackResult);
        return rollBackResult;
    }

    @Override
    public FlowableExecuteVo getExecuteInfoByProcessId(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (processInstance==null){
            throw new CommonException("流程已完成或不存在");
        }
        // 获取流程定义
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        Process mainProcess = bpmnModel.getMainProcess();
        Collection<FlowElement> flowElements = mainProcess.getFlowElements();
        if (CollectionUtil.isEmpty(flowElements)){
            throw new CommonException("流程定义不存在");
        }
        if (StrUtil.isBlank(processInstanceId)){
            throw new CommonException("流程id为空");
        }
        List<TaskInfoVo> taskInfoVoList = processActiveTaskWithoutData(processInstanceId);
        if (CollectionUtil.isEmpty(taskInfoVoList)){
            throw new CommonException("无需执行审批");
        }

        List<FlowElement> sourceFlowElementList=new ArrayList<>();
        for (TaskInfoVo taskInfoVo:taskInfoVoList){
            Execution execution = runtimeService
                    .createExecutionQuery()
                    .processInstanceId(processInstanceId)
                    .executionId(taskInfoVo.getExecutionId()).singleResult();
            if (execution==null){
                throw new CommonException("流程执行节点不存在");
            }
            FlowNode flowNode = (FlowNode) mainProcess.getFlowElement(execution.getActivityId());
            sourceFlowElementList.add(flowNode);
        }
        Map<String, FlowNode> upstreamFlowNode=new HashMap<>();
        FlowableUtil.getAllUpstreamFlowNode(sourceFlowElementList,upstreamFlowNode);


        FlowableExecuteVo flowableExecuteVo=new FlowableExecuteVo();
        flowableExecuteVo.setTaskIdAndUserNameMapping(taskInfoVoList.stream().collect(Collectors.toMap(TaskInfoVo::getId,TaskInfoVo::getName)));
        flowableExecuteVo.setTaskIdAndUserIdMapping(taskInfoVoList.stream().collect(Collectors.toMap(TaskInfoVo::getId,TaskInfoVo::getAssignee)));

        Map<String, String> allowRollBackIdAndNameMapping = upstreamFlowNode.entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .filter(e-> !(e.getValue() instanceof Gateway))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    FlowNode flowNode = e.getValue();
                    if (flowNode instanceof StartEvent){
                        return "开始节点";
                    }else {
                        String name = flowNode.getName();
                        if (StrUtil.isBlank(name)){
                            return flowNode.getId();
                        }else {
                            return name;
                        }
                    }
                }));
        flowableExecuteVo
                .setAllowRollBackIdAndNameMapping(allowRollBackIdAndNameMapping);


        List<TcUser> userList = userService.getUserList();
        Map<String, String> userIdAndNameMapping= userList.stream().collect(Collectors.toMap(TcUser::getId,TcUser::getNickName));
        flowableExecuteVo.setUserIdAndNameMapping(userIdAndNameMapping);

        return flowableExecuteVo;
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public String createTestProcess(CreateTestProcessInstance createTestProcessInstance) {
        String businessKey = createTestProcessInstance.getBusinessKey();
        String processDefinitionKey = createTestProcessInstance.getProcessDefinitionKey();
        List<VariableData> variableDataList = createTestProcessInstance.getVariableDataList();
        if (StrUtil.isBlank(businessKey)){
            throw new CommonException("businessKey不能为空");
        }
        if (StrUtil.isBlank(processDefinitionKey)){
            throw new CommonException("流程定义不能为空");
        }
        return LockUtil.tryLock(processDefinitionKey+businessKey, "正在执行操作中,请稍后再试", () -> {
            String listenerClassName = createTestProcessInstance.getListenerClassName();
            if (StrUtil.isBlank(listenerClassName)){
                listenerClassName= FlowableTestTipListener.class.getName();
            }
            Map<String, Object> variables = new HashMap<>();
            if (CollectionUtil.isNotEmpty(variableDataList)){
                variables=variableDataList.stream().collect(Collectors.groupingBy(VariableData::getKey,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            VariableData variableData = list.get(0);
                            return VariableDataTypeEnum.convertStrValueToObjValue(variableData);
                        })));
            }
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .active()
                    .processDefinitionKey(processDefinitionKey)
                    .processInstanceBusinessKey(businessKey)
                    .singleResult();
            String processInstanceId;
            if (processInstance!=null){
                processInstanceId = processInstance.getId();
            }else {
                HistoricProcessInstance historicProcessInstance = historyService
                        .createHistoricProcessInstanceQuery()
                        .processDefinitionKey(processDefinitionKey)
                        .processInstanceBusinessKey(businessKey)
                        .singleResult();
                if (historicProcessInstance!=null){
                    processInstanceId = historicProcessInstance.getId();
                }else {
                    String name = "测试_"+businessKey;
                    StartProcessInstance startProcessInstance = new StartProcessInstance();
                    startProcessInstance.setProcessDefinitionKey(processDefinitionKey);
                    startProcessInstance.setBusinessNo(businessKey);
                    startProcessInstance.setBusinessKey(businessKey);
                    startProcessInstance.setBusinessType("test");
                    startProcessInstance.setJumpUrl(businessKey);
                    startProcessInstance.setId(businessKey);
                    startProcessInstance.setTitle(name);
                    startProcessInstance.setName(name);
                    if (StrUtil.isNotBlank(listenerClassName)){
                        try {
                            @SuppressWarnings("unchecked")
                            Class<? extends FlowableTaskListener> listenerClass =(Class<? extends FlowableTaskListener>) Class.forName(listenerClassName);
                            startProcessInstance.setListener(listenerClass);
                        } catch (ClassNotFoundException e) {
                            String msg=StrUtil.format("监听器类错误或不存在->{}",ExceptionUtil.stacktraceToString(e));
                            throw new CommonException(msg);
                        }
                    }
                    startProcessInstance.setData(variables);

                    processInstance = startProcess(startProcessInstance);
                    processInstanceId=processInstance.getId();
                }
            }
            return processInstanceId;
        },(e)->{
            log.error("启动测试流程失败->{}", ExceptionUtil.stacktraceToString(e));
            throw new CommonException(StrUtil.format("系统异常,启动测试流程失败->{}",e.getMessage()));
        });
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public String executeProcess(ExecuteProcessInstance executeProcessInstance) {
        String taskId = executeProcessInstance.getTaskId();
        String executeType = executeProcessInstance.getExecuteType();
        String transferUserId = executeProcessInstance.getTransferUserId();
        List<String> rollbackTargetActIdList = executeProcessInstance.getRollbackTargetActIdList();
        String processInstanceId = executeProcessInstance.getProcessInstanceId();
        String msg = executeProcessInstance.getMsg();
        if (StrUtil.isBlank(taskId)){
            throw new CommonException("taskId不能为空");
        }
        if (executeType==null){
            throw new CommonException("type不能为空");
        }
        String userId = executeProcessInstance.getUserId();
        if (StrUtil.isBlank(userId)){
            throw new CommonException("userId不能为空");
        }
        TcUser user = userService.selectById(userId);
        if (user==null){
            throw new CommonException("用户不存在");
        }
        ExecuteTask executeTask=new ExecuteTask(processInstanceId,taskId,user,executeType, msg);
        if (TaskStatusEnum.rollback.name().equals(executeType)||TaskStatusEnum.rollBackRecordMsg.name().equals(executeType)){
            executeTask.setRollbackTargetActIdList(rollbackTargetActIdList);
        }else if (TaskStatusEnum.transfer.name().equals(executeType)){
            if (StrUtil.isBlank(transferUserId)){
                throw new CommonException("转办人员不能为空");
            }
            executeTask.setTransferUserId(transferUserId);
        }
        executeTask(executeTask);
        return "操作成功";
    }

    @Override
    public List<ProcessVariableVo> getProcessVariableList(String processInstanceId) {
        if (StrUtil.isBlank(processInstanceId)){
            throw new CommonException("流程ID为空");
        }
        ProcessInstanceVo processInstanceVo = getProcessInstanceVoById(processInstanceId);
        if (processInstanceVo==null){
            return new ArrayList<>();
        }
        Map<String, Object> processVariables=processInstanceVo.getProcessVariables();
        List<ProcessVariableVo> variableVoList=new ArrayList<>();
        for (Map.Entry<String,Object> entry:processVariables.entrySet()){
            String key = entry.getKey();
            String value = Optional.ofNullable(entry.getValue()).map(Object::toString).orElse("");
            variableVoList.add(new ProcessVariableVo(key,value));
        }
        return variableVoList;
    }

    @Override
    public List<TaskVariableVo> getTaskVariableList(String processInstanceId) {
        if (StrUtil.isBlank(processInstanceId)){
            throw new CommonException("流程ID为空");
        }
        List<TaskInfoVo> taskInfoVoList = processAllTask(processInstanceId);
        Map<String, TaskVariableVo> taskVariableVoMap=taskInfoVoList.stream()
                .collect(Collectors.groupingBy(TaskInfoVo::getId,
                        Collectors.collectingAndThen(Collectors.toList(),list->{

                            TaskInfoVo taskInfoVo = list.get(0);
                            TaskVariableVo taskVariableVo=new TaskVariableVo();
                            taskVariableVo.setTaskId(taskInfoVo.getId());
                            taskVariableVo.setName(taskInfoVo.getName());
                            Map<String, Object> taskLocalVariables = taskInfoVo.getTaskLocalVariables();
                            if (taskLocalVariables==null){
                                taskLocalVariables=new HashMap<>();
                            }
                            taskVariableVo.setVariableList(taskLocalVariables.entrySet()
                                    .stream().map(entryItem-> new TaskVariableDetailVo(entryItem.getKey(),
                                            Optional.ofNullable(entryItem.getValue()).map(Object::toString).orElse("")))
                                    .collect(Collectors.toList()));
                            return taskVariableVo;
                        })));
        return new ArrayList<>(taskVariableVoMap.values());
    }

    @Override
    public String saveOrUpdateProcessVariable(String processInstanceId, VariableData variableData) {
        String key = variableData.getKey();
        Object value = VariableDataTypeEnum.convertStrValueToObjValue(variableData);
        if (StrUtil.isBlank(processInstanceId)){
            throw new CommonException("流程ID不能为空");
        }
        if (StrUtil.isBlank(key)){
            throw new CommonException("流程变量Key不能为空");
        }
        ProcessInstanceVo processInstanceVo = getProcessInstanceVoById(processInstanceId);
        if (processInstanceVo!=null&&!processInstanceVo.isHis()){
            runtimeService.setVariable(processInstanceId,key,value);
            return "操作成功";
        }else {
            //todo 暂不实现
            throw new CommonException("流程已结束或不存在，无法新增修改变量");
        }
    }

    @Override
    public String saveOrUpdateTaskVariable(String processInstanceId,String taskId, VariableData variableData) {
        String key = variableData.getKey();
        Object value = VariableDataTypeEnum.convertStrValueToObjValue(variableData);
        if (StrUtil.isBlank(taskId)){
            throw new CommonException("任务ID不能为空");
        }
        if (StrUtil.isBlank(key)){
            throw new CommonException("任务变量Key不能为空");
        }
        TaskInfoVo taskInfoVo = getTaskInfoVoById(processInstanceId, taskId);
        if (taskInfoVo!=null&&!taskInfoVo.isHis()){
            taskService.setVariableLocal(taskId,key,value);
            return "操作成功";
        }else {
            //todo 暂不实现
            throw new CommonException("任务已结束或不存在，无法新增修改变量");
        }
    }

    @Override
    public String deleteProcessVariable(String processInstanceId, VariableData variableData) {
        String key = variableData.getKey();
        if (StrUtil.isBlank(processInstanceId)){
            throw new CommonException("流程ID不能为空");
        }
        if (StrUtil.isBlank(key)){
            throw new CommonException("流程变量Key不能为空");
        }
        ProcessInstanceVo processInstanceVo = getProcessInstanceVoById(processInstanceId);
        if (processInstanceVo!=null&&!processInstanceVo.isHis()){
            runtimeService.removeVariable(processInstanceId,key);
            return "操作成功";
        }else {
            //todo 暂不实现
            throw new CommonException("流程已结束或不存在，无法删除变量");
        }
    }

    @Override
    public String deleteTaskVariable(String processInstanceId,String taskId, VariableData variableData) {
        String key = variableData.getKey();
        if (StrUtil.isBlank(taskId)){
            throw new CommonException("任务ID不能为空");
        }
        if (StrUtil.isBlank(key)){
            throw new CommonException("任务变量Key不能为空");
        }
        TaskInfoVo taskInfoVo = getTaskInfoVoById(processInstanceId, taskId);
        if (taskInfoVo!=null&&!taskInfoVo.isHis()){
            taskService.removeVariableLocal(taskId,key);
            return "操作成功";
        }else{
            //todo 暂不实现
            throw new CommonException("任务已结束或不存在，无法删除变量");
        }
    }

    /**
     * 获取流程图
     */
    private InputStream getProcessImage(String processInstanceId) {
        ProcessInstanceVo processInstanceVo = getProcessInstanceVoById(processInstanceId);
        if (processInstanceVo==null){
            return null;
        }
        String procDefId=processInstanceVo.getProcessDefinitionId();
        Map<String,Object> variables=processInstanceVo.getProcessVariables();


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

        //获取所有审批通过task映射
//        List<HistoricTaskInstance> hisList = historyService.createHistoricTaskInstanceQuery()
//                .processInstanceId(processInstanceId)
//                .finished().taskWithoutDeleteReason().list();
//        Map<String, List<Task>> actIdAndTaskMap = list.stream().collect(Collectors.groupingBy(TaskInfo::getTaskDefinitionKey));
        for (FlowElement flowElement:flowElements){
            if (flowElement instanceof UserTask){
                UserTask userTask= (UserTask) flowElement;
                String name = userTask.getName();
                if (StrUtil.isNotBlank(name)&&name.startsWith("${")){
                    name=getExpressionValue(variables,name).toString();
                }else {
//                    String id = userTask.getId();
//                    if (!FlowableUtil.isMultiInstanceTaskName(name)){
//                        List<Task> tasks = actIdAndTaskMap.get(id);
//                        if (CollectionUtil.isNotEmpty(tasks)){
//                            Task task = tasks.get(0);
//                            String assignee = task.getAssignee();
//                            TcUser user = userService.selectById(assignee);
//                            if (user!=null){
//                                name=user.getNickName();
//                            }
//                        }
//                    }
                }
                userTask.setName(name);
            }
        }

        String activityFontName = "宋体"; // 节点字体
        String labelFontName = "微软雅黑"; // 连线标签字体
        String annotationFontName = "宋体"; // 连线标签字体
//        ClassLoader customClassLoader = null; // 类加载器
        double scaleFactor = 2.0d; // 比例因子，默认即可
        boolean drawSequenceFlowNameWithNoLabelDI = false; // 连线标签
        // 生成图片
        return defaultProcessDiagramGenerator.generateDiagram(bpmnModel, imageType, highLightedActivities
                , highLightedFlows, activityFontName, labelFontName, annotationFontName, null,
                scaleFactor, drawSequenceFlowNameWithNoLabelDI);
    }
}
