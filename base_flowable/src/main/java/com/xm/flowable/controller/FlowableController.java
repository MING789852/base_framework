package com.xm.flowable.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.advice.exception.exception.CommonException;
import com.xm.module.core.params.QueryData;
import com.xm.core.params.Result;
import com.xm.flowable.domain.entity.TcFlowableModel;
import com.xm.flowable.domain.query.ProcessInstancePageQuery;
import com.xm.flowable.domain.vo.FlowableModelVo;
import com.xm.flowable.domain.vo.ProcessInstanceVo;
import com.xm.flowable.domain.vo.ProcessView;
import com.xm.flowable.domain.vo.TaskInfoVo;
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
}
