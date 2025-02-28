package com.xm.flowable.service;


import com.xm.auth.domain.entity.TcUser;
import com.xm.flowable.domain.vo.ProcessInstanceVo;
import com.xm.flowable.domain.vo.TaskInfoVo;
import com.xm.flowable.domain.dto.ExecuteTask;
import com.xm.flowable.domain.dto.FlowableSimpleBpmnDeployment;
import com.xm.flowable.domain.dto.StartProcessInstance;
import com.xm.flowable.enums.TaskStatusEnum;
import org.flowable.engine.runtime.ProcessInstance;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface FlowableService {
    /**
     * 创建简单流程，只有并行和串行流程
     */
    void createSimpleDeployment(FlowableSimpleBpmnDeployment flowableSimpleBpmnDeployment);

    /**
     *是否存在流程定义
     */
    boolean existDeploymentByProcessDefinitionKey(String processDefinitionKey);


    /**
     * 是否存在流程实例
     */
    boolean existProcessInstanceByProcessDefinitionKey(String processDefinitionKey);


    /**
     * 启动流程
     */
    ProcessInstance startProcess(StartProcessInstance startProcessInstance);

    /**
     * 查看所有待办任务和已办任务（展示流程图节点）
     */
    List<TaskInfoVo> processAllTask(String processInstanceId);

    List<TaskInfoVo> processAllTaskWithoutData(String processInstanceId);

    /**
     * 查看当前还存在待办任务（不携带审批流数据）
     */
    List<TaskInfoVo> processActiveTaskWithoutData(String processInstanceId);

    /**
     * 查看当前是否还存在待办任务，如果不存在则说明已完结（不携带审批流数据）
     */
    long processActiveTaskCountWithoutData(String processInstanceId);

    /**
     * 查询当前人员在流程中是否存在待办
     */
    List<TaskInfoVo> getTaskByUserAndProcessId(TcUser user,String processInstanceId);

    /**
     * 查询当前人员在流程中是否存在待办(数量)
     */
    long getTaskCountByUserAndProcessId(TcUser user,String processInstanceId);

    /**
     * 执行审批任务
     */
    void executeTask(ExecuteTask executeTask);

    /**
     * 激活流程
     */
    boolean activityProcess(String processInstanceId);

    /**
     * 中止流程
     */
    void suspendProcess(String processInstanceId);

    void deleteSuspendProcess(String processInstanceId);

    /**
     * 删除流程
     */
    void deleteProcess(String processInstanceId);

    /**
     * 删除流程定义
     */
    void deleteDeploymentById(String deploymentId);

    /**
     * 删除流程定义
     */
    void deleteDeploymentByKey(String key);

    /**
     * 查看流程图（base64）
     */
    String viewProcessImageBase64(String processInstanceId);

    /**
     * 获取表达式计算值
     */
    Object getExpressionValue(Map<String, Object> variables, String expression);

    /**
     * 转办
     */
    void transfer(String taskId, String assignee);


    /**
     * 查看流程图
     */
    boolean viewProcessImageByProcessId(String processInstanceId, HttpServletResponse response);
}
