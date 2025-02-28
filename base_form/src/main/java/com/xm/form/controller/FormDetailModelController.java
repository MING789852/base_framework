package com.xm.form.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.module.core.params.QueryData;
import com.xm.core.params.Result;
import com.xm.form.domain.entity.TcFormDetailModel;
import com.xm.form.service.TcFormDetailModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("formDetailModel")
public class FormDetailModelController {

    private final TcFormDetailModelService formDetailModelService;

    @PostMapping("selectByPage")
    public Result<Page<TcFormDetailModel>> selectByPage(@RequestBody QueryData<TcFormDetailModel> queryData){
        return Result.successForData(formDetailModelService.selectByPage(queryData));
    }

    @PostMapping("deleteData")
    public Result<String> deleteData(@RequestBody List<TcFormDetailModel> list){
        return Result.successForData(formDetailModelService.deleteData(list));
    }

    @PostMapping("saveOrUpdateData")
    public Result<String> saveOrUpdateData(@RequestBody List<TcFormDetailModel> list){
        return Result.successForData(formDetailModelService.saveOrUpdateData(list));
    }

    @GetMapping("getDictMapping")
    public Result<Map<String,Object>> getDictMapping(){
        return Result.successForData(formDetailModelService.getDictMapping());
    }
}
