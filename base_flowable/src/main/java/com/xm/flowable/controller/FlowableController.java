package com.xm.flowable.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.advice.exception.exception.CommonException;
import com.xm.flowable.domain.dto.ChangeModel;
import com.xm.flowable.domain.dto.CreateTestProcessInstance;
import com.xm.flowable.domain.dto.ExecuteProcessInstance;
import com.xm.flowable.domain.dto.VariableData;
import com.xm.flowable.domain.vo.*;
import com.xm.module.core.params.QueryData;
import com.xm.core.params.Result;
import com.xm.flowable.domain.entity.TcFlowableModel;
import com.xm.flowable.domain.query.ProcessInstancePageQuery;
import com.xm.flowable.service.FlowableModelService;
import com.xm.flowable.service.FlowableService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("flowable")
@RequiredArgsConstructor
public class FlowableController {

    private final FlowableService flowableService;

    private final FlowableModelService flowableModelService;

    @GetMapping("viewProcess")
    public Result<ProcessView> viewProcess(String processId){
        if (StrUtil.isBlank(processId)){
            throw new CommonException("id不存在，无法查看");
        }
        String base64 = flowableService.viewProcessImageBase64(processId);
        List<TaskInfoVo> taskInfoVos = flowableService.processAllTask(processId);

        ProcessView processView=new ProcessView();
        processView.setBase64(base64);
        processView.setTaskInfoVoList(taskInfoVos);
        return Result.successForData(processView);
    }

    @PostMapping("saveModel")
    public Result<String> saveModel(@RequestBody FlowableModelVo flowableModelVo){
        return Result.successForData(flowableModelService.saveModel(flowableModelVo));
    }

    @PostMapping("changeModelNode")
    public Result<String> changeModelNode(@RequestBody ChangeModel changeModel){
        return Result.successForData(flowableModelService.changeModelNode(changeModel));
    }

    @PostMapping("changeModelEdge")
    public Result<String> changeModelEdge(@RequestBody ChangeModel changeModel){
        return Result.successForData(flowableModelService.changeModelEdge(changeModel));
    }


    @PostMapping("fillModel")
    public Result<FlowableModelVo> fillModel(@RequestBody TcFlowableModel flowableModel){
        return Result.successForData(flowableModelService.fillModel(flowableModel));
    }

    @PostMapping("deleteData")
    public Result<String> deleteData(@RequestBody List<TcFlowableModel> list){
        return Result.successForData(flowableModelService.deleteData(list));
    }

    @PostMapping("forceDeleteData")
    public Result<String> forceDeleteData(@RequestBody List<TcFlowableModel> list){
        return Result.successForData(flowableModelService.forceDeleteData(list));
    }

    @PostMapping("selectByPage")
    public Result<Page<TcFlowableModel>> selectByPage(@RequestBody QueryData<TcFlowableModel> queryData){
        return Result.successForData(flowableModelService.selectByPage(queryData));
    }


    @PostMapping("getProcInsPageByProcDefKey")
    public Result<Page<ProcessInstanceVo>> getProcInsPageByProcDefKey(@RequestBody ProcessInstancePageQuery query){
        return Result.successForData(flowableModelService.getProcInsPageByProcDefKey(query));
    }


    @PostMapping("deleteProcessInstance")
    public Result<String> deleteProcessInstance(@RequestBody List<ProcessInstanceVo>  processInstanceVoList){
        return Result.successForData(flowableModelService.deleteProcessInstance(processInstanceVoList));
    }


    @GetMapping("getDictList")
    public Result<Map<String,Object>> getDictList(){
        return Result.successForData(flowableModelService.getDictList());
    }

    @GetMapping("initUnRecordModel")
    public Result<String> initUnRecordModel(){
        return Result.successForData(flowableModelService.initUnRecordModel());
    }

    @GetMapping("getExecuteInfoByProcessId")
    public Result<FlowableExecuteVo> getExecuteInfoByProcessId(String processInstanceId){
        return Result.successForData(flowableService.getExecuteInfoByProcessId(processInstanceId));
    }

    @PostMapping("createTestProcess")
    public Result<String> createTestProcess(@RequestBody CreateTestProcessInstance createTestProcessInstance){
        return Result.successForData(flowableService.createTestProcess(createTestProcessInstance));
    }

    @PostMapping("executeProcess")
    public Result<String> executeProcess(@RequestBody ExecuteProcessInstance executeProcessInstance){
        return Result.successForData(flowableService.executeProcess(executeProcessInstance));
    }

    @PostMapping("reSendApproveMsg")
    public Result<String> reSendApproveMsg(@RequestBody List<ProcessInstanceVo> processInstanceVoList){
        return Result.successForData(flowableModelService.reSendApproveMsg(processInstanceVoList));
    }

    @GetMapping("getProcessVariableList")
    public Result<List<ProcessVariableVo>> getProcessVariableList(String processInstanceId){
        return Result.successForData(flowableService.getProcessVariableList(processInstanceId));
    }

    @GetMapping("getTaskVariableList")
    public Result<List<TaskVariableVo>> getTaskVariableList(String processInstanceId){
        return Result.successForData(flowableService.getTaskVariableList(processInstanceId));
    }

    @PostMapping("saveOrUpdateProcessVariable")
    public Result<String> saveOrUpdateProcessVariable(String processInstanceId,@RequestBody VariableData variableData){
        return Result.successForData(flowableService.saveOrUpdateProcessVariable(processInstanceId,variableData));
    }

    @PostMapping("saveOrUpdateTaskVariable")
    public Result<String> saveOrUpdateTaskVariable(String processInstanceId,String taskId,@RequestBody VariableData variableData){
        return Result.successForData(flowableService.saveOrUpdateTaskVariable(processInstanceId,taskId,variableData));
    }

    @PostMapping("deleteProcessVariable")
    public Result<String> deleteProcessVariable(String processInstanceId,@RequestBody VariableData variableData){
        return Result.successForData(flowableService.deleteProcessVariable(processInstanceId,variableData));
    }

    @PostMapping("deleteTaskVariable")
    public Result<String> deleteTaskVariable(String processInstanceId,String taskId,@RequestBody VariableData variableData){
        return Result.successForData(flowableService.deleteTaskVariable(processInstanceId,taskId,variableData));
    }
}
