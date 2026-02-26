package com.xm.flowable.domain.dto;

import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.flowable.enums.TaskExecuteActionTypeEnum;
import com.xm.flowable.enums.TaskStatusEnum;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExecuteTask {
    private String processInstanceId;
    private TaskStatusEnum status;
    private String msg;
    private TaskExecuteActionTypeEnum executeActionType;

    //当前用户执行
    public ExecuteTask(String processInstanceId,TaskStatusEnum status,String msg) {
        this.processInstanceId = processInstanceId;
        this.status = status;
        this.msg = msg;
        this.executeActionType = TaskExecuteActionTypeEnum.currentUser;
        this.setDefaultMsg();
    }
    public ExecuteTask(String processInstanceId,String statusName,String msg) {
        TaskStatusEnum status = TaskStatusEnum.getEnumByName(statusName);
        if (status==null){
            throw new CommonException("非法任务执行状态");
        }
        this.processInstanceId = processInstanceId;
        this.status = status;
        this.msg = msg;
        this.executeActionType = TaskExecuteActionTypeEnum.currentUser;
        this.setDefaultMsg();
    }

    //指定用户执行
    private String taskId;
    private TcUser user;

    public ExecuteTask(String processInstanceId,String taskId,TcUser user,TaskStatusEnum status,String msg) {
        this.processInstanceId = processInstanceId;
        this.taskId = taskId;
        this.user = user;
        this.status = status;
        this.msg = msg;
        this.executeActionType = TaskExecuteActionTypeEnum.customUser;
        this.setDefaultMsg();
    }
    public ExecuteTask(String processInstanceId,String taskId,TcUser user,String statusName,String msg) {
        TaskStatusEnum status = TaskStatusEnum.getEnumByName(statusName);
        if (status==null){
            throw new CommonException("非法任务执行状态");
        }
        this.processInstanceId = processInstanceId;
        this.taskId = taskId;
        this.user = user;
        this.status = status;
        this.msg = msg;
        this.executeActionType = TaskExecuteActionTypeEnum.customUser;
        this.setDefaultMsg();
    }

    //设置任务变量
    private Map<String,Object> variable;
    //回滚目标，若无则使用直接回滚
    private List<String> rollbackTargetActIdList;
    //转办人员
    private String transferUserId;

    //设置默认审批信息
    private void setDefaultMsg(){
        if (StrUtil.isBlank(this.msg)){
            if (TaskStatusEnum.success.equals(this.status)){
                msg="同意";
            }else if (TaskStatusEnum.reject.equals(this.status)){
                msg="拒绝";
            }else if (TaskStatusEnum.suspend.equals(this.status)){
                msg="中止";
            }else if (TaskStatusEnum.rollBackRecordMsg.equals(this.status)){
                msg="回退";
            }
        }
    }
}
