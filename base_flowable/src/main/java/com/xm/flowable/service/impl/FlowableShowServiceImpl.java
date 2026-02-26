package com.xm.flowable.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.auth.domain.entity.TcUser;
import com.xm.flowable.domain.params.FlowableMeta;
import com.xm.flowable.domain.query.FlowableShowDataQuery;
import com.xm.flowable.domain.vo.FlowableShowDataVo;
import com.xm.flowable.domain.vo.ProcessInstanceVo;
import com.xm.flowable.domain.vo.TodoItemVo;
import com.xm.flowable.enums.FlowableShowTypeEnum;
import com.xm.flowable.enums.ProcessVariableNameEnum;
import com.xm.flowable.listener.FlowableTaskListener;
import com.xm.flowable.service.FlowableShowService;
import com.xm.util.FlowableUtil;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.http.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowableShowServiceImpl implements FlowableShowService {

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final HistoryService historyService;

    private boolean getAllToDoList(){
        boolean showAll = UserInfoUtil.haveRouterAction("flowable-show", "showAll");
        boolean isSuperPrivilege = UserInfoUtil.isSuperPrivilege();
        return showAll || isSuperPrivilege;
    }

    @Override
    public Page<FlowableShowDataVo> getData(FlowableShowDataQuery query) {
        Map<String, FlowableTaskListener> beanMapOfType = SpringBeanUtil.getBeanMapOfType(FlowableTaskListener.class);
        List<FlowableMeta> metaList=new ArrayList<>();
        if (CollectionUtil.isNotEmpty(beanMapOfType)) {
            metaList=beanMapOfType.values().stream().flatMap(item->item.getMeta().stream()).collect(Collectors.toList());
        }

        Integer finish = query.getFinish();
        String businessType = query.getBusinessType();
        int size = query.getSize();
        int current = query.getCurrent();

        Page<FlowableShowDataVo> page=new Page<>(current,size);

        Map<String, FlowableMeta> metaMap = metaList.stream().collect(Collectors.toMap(FlowableMeta::getBusinessType, Function.identity()));


        if (finish==null){
            finish=0;
        }
        boolean all = getAllToDoList();
        TcUser user = UserInfoUtil.getCurrentLoginUserBySessionOrTokenNotNull();

        List<ProcessInstanceVo> processInstanceVoList=new ArrayList<>();
        List<FlowableShowDataVo> resultList=new ArrayList<>();
        long total=0;
        page.setTotal(total);
        page.setRecords(resultList);
        if (finish==0){
            ProcessInstanceQuery activeQuery = runtimeService.createProcessInstanceQuery()
                    .includeProcessVariables()
                    .active();
            if (!all){
                List<Task> list = taskService.createTaskQuery()
                        .active()
                        .taskAssignee(user.getId())
                        .list();
                if (CollectionUtil.isEmpty(list)){
                    return page;
                }
                activeQuery.processInstanceIds(list.stream().map(Task::getProcessInstanceId).collect(Collectors.toSet()));
            }
            if (StrUtil.isNotBlank(businessType)) {
                activeQuery.variableValueEquals(ProcessVariableNameEnum.businessType.name(),businessType);
            }
            total = activeQuery.count();
            List<ProcessInstance> processInstances = activeQuery.listPage(current-1, size);
            if (CollectionUtil.isNotEmpty(processInstances)) {
                processInstanceVoList=processInstances.stream().map(FlowableUtil::procInsConvertProcInsVo).collect(Collectors.toList());
            }
        }else {
            HistoricProcessInstanceQuery finishedQuery = historyService.createHistoricProcessInstanceQuery()
                    .includeProcessVariables()
                    .finished();
            if (!all){
                List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                        .finished()
                        .taskAssignee(user.getId())
                        .list();
                if (CollectionUtil.isEmpty(list)){
                    return page;
                }
                finishedQuery.processInstanceIds(list.stream().map(HistoricTaskInstance::getProcessInstanceId).collect(Collectors.toSet()));
            }
            if (StrUtil.isNotBlank(businessType)) {
                finishedQuery.variableValueEquals(ProcessVariableNameEnum.businessType.name(),businessType);
            }
            total = finishedQuery.count();
            List<HistoricProcessInstance> processInstances = finishedQuery.listPage(current-1, size);
            if (CollectionUtil.isNotEmpty(processInstances)) {
                processInstanceVoList=processInstances.stream().map(FlowableUtil::hisProcInsConvertProcInsVo).collect(Collectors.toList());
            }
        }

        //处理数据
        page.setTotal(total);
        page.setRecords(resultList);
        for (ProcessInstanceVo processInstanceVo : processInstanceVoList) {
            Map<String, Object> processVariables = processInstanceVo.getProcessVariables();
            FlowableShowDataVo dataVo=new FlowableShowDataVo();
            String creator =FlowableUtil.getMapVariable(processVariables,ProcessVariableNameEnum.creator.name());
            String businessTypeItem=FlowableUtil.getMapVariable(processVariables,ProcessVariableNameEnum.businessType.name());
            String businessKey=FlowableUtil.getMapVariable(processVariables,ProcessVariableNameEnum.businessKey.name());

            dataVo.setEndDate(processInstanceVo.getEndTime());
            dataVo.setStartDate(processInstanceVo.getStartTime());
            dataVo.setCreateUser(creator);
            dataVo.setInstance(processInstanceVo);
            dataVo.setFinish(processInstanceVo.getEndTime()==null?"未完成":"已完成");
            dataVo.setBusinessType(businessTypeItem);

            FlowableMeta meta = metaMap.get(businessTypeItem);
            if (meta!=null){
                dataVo.setFlowableName(meta.getFlowableName());
                String jumpUrl = meta.getJumpUrl();
                Map<String,String> queryMap=new HashMap<>();
                queryMap.put("id",businessKey);
                queryMap.put("type", FlowableShowTypeEnum.viewDetail.name());
                jumpUrl=HttpUtils.buildUrl(jumpUrl,queryMap);
                dataVo.setJumpUrl(jumpUrl);
            }
            resultList.add(dataVo);
        }
        return page;
    }

    @Override
    public Map<String, Object> getDictMapping() {
        Map<String, Object> result = new HashMap<>();
        //查询类型
        Map<String, FlowableTaskListener> beanMapOfType = SpringBeanUtil.getBeanMapOfType(FlowableTaskListener.class);
        List<FlowableMeta> metaList=new ArrayList<>();
        if (CollectionUtil.isNotEmpty(beanMapOfType)) {
            metaList=beanMapOfType.values().stream().flatMap(item->item.getMeta().stream()).collect(Collectors.toList());
        }
        Map<String,String> businessTypeMap=metaList.stream().
                collect(Collectors.toMap(FlowableMeta::getBusinessType, FlowableMeta::getFlowableName, (key1, key2) -> key1));
        result.put("businessType", businessTypeMap);
        return result;
    }

    @Override
    public List<TodoItemVo> getTodoList() {
        //查询数据
        boolean all = getAllToDoList();
        TcUser user = UserInfoUtil.getCurrentLoginUserBySessionOrTokenNotNull();
        ProcessInstanceQuery activeQuery = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .active();
        List<TodoItemVo> todoItemVoList=new ArrayList<>();
        if (!all){
            List<Task> list = taskService.createTaskQuery()
                    .active()
                    .taskAssignee(user.getId())
                    .list();
            if (CollectionUtil.isNotEmpty(list)){
                activeQuery.processInstanceIds(list.stream().map(Task::getProcessInstanceId).collect(Collectors.toSet()));
            }else {
                return todoItemVoList;
            }
        }
        List<ProcessInstance> list = activeQuery.list();
        List<ProcessInstanceVo> processInstanceVo = list.stream().map(FlowableUtil::procInsConvertProcInsVo).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(processInstanceVo)){
            return todoItemVoList;
        }

        //查询类型
        Map<String, FlowableTaskListener> beanMapOfType = SpringBeanUtil.getBeanMapOfType(FlowableTaskListener.class);
        List<FlowableMeta> metaList=new ArrayList<>();
        if (CollectionUtil.isNotEmpty(beanMapOfType)) {
            metaList=beanMapOfType.values().stream().flatMap(item->item.getMeta().stream()).collect(Collectors.toList());
        }
        Map<String,String> businessTypeMap=metaList.stream().
                collect(Collectors.toMap(FlowableMeta::getBusinessType, FlowableMeta::getFlowableName, (key1, key2) -> key1));

        Map<String, Long> businessTypeCountMap = processInstanceVo.stream().collect(Collectors.groupingBy(item ->
                Optional.ofNullable(item.getBusinessType()).orElse(""),Collectors.counting()));

        for (Map.Entry<String, String> entry : businessTypeMap.entrySet()){
            String businessType = entry.getKey();
            String flowableName = entry.getValue();
            Long count = Optional.ofNullable(businessTypeCountMap.get(businessType)).orElse(0L);
            if (count.intValue()==0){
                continue;
            }

            TodoItemVo todoItemVo=new TodoItemVo();
            todoItemVo.setTagContent(count.toString());
            todoItemVo.setContent(flowableName);
            todoItemVo.setBusinessType(businessType);
            todoItemVoList.add(todoItemVo);
        }

        return todoItemVoList;
    }
}
