package com.xm.form.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.module.core.params.QueryData;
import com.xm.core.params.Result;
import com.xm.form.domain.entity.TcFormMainModel;
import com.xm.form.domain.query.FormModelQuery;
import com.xm.form.domain.vo.FormModelResult;
import com.xm.form.service.TcFormMainModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("formMainModel")
public class FormMainModelController {

    private final TcFormMainModelService formMainModelService;

    @PostMapping("selectByPage")
    public Result<Page<TcFormMainModel>> selectByPage(@RequestBody QueryData<TcFormMainModel> queryData){
        return Result.successForData(formMainModelService.selectByPage(queryData));
    }

    @PostMapping("deleteData")
    public Result<String> deleteData(@RequestBody List<TcFormMainModel> list){
        return Result.successForData(formMainModelService.deleteData(list));
    }

    @PostMapping("saveOrUpdateData")
    public Result<String> saveOrUpdateData(@RequestBody List<TcFormMainModel> list){
        return Result.successForData(formMainModelService.saveOrUpdateData(list));
    }

    @PostMapping("getFormModelResultById")
    public Result<FormModelResult> getFormModelResultById(@RequestBody FormModelQuery query){
        return Result.successForData(formMainModelService.getFormModelResultById(query));
    }
}
