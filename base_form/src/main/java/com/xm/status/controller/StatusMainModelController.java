package com.xm.status.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.module.core.params.QueryData;
import com.xm.core.params.Result;
import com.xm.status.domain.dto.StatusChangeAddDto;
import com.xm.status.domain.dto.TcStatusDetailInstanceDto;
import com.xm.status.domain.entity.TcStatusMainModel;
import com.xm.status.domain.query.StatusModelQuery;
import com.xm.status.domain.vo.StatusModelDataResult;
import com.xm.status.service.TcStatusMainModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("statusMainModel")
public class StatusMainModelController {

    private final TcStatusMainModelService statusMainModelService;

    @PostMapping("selectByPage")
    public Result<Page<TcStatusMainModel>> selectByPage(@RequestBody QueryData<TcStatusMainModel> queryData){
        return Result.successForData(statusMainModelService.selectByPage(queryData));
    }

    @PostMapping("deleteData")
    public Result<String> deleteData(@RequestBody List<TcStatusMainModel> list){
        return Result.successForData(statusMainModelService.deleteData(list));
    }

    @PostMapping("saveOrUpdateData")
    public Result<String> saveOrUpdateData(@RequestBody List<TcStatusMainModel> list){
        return Result.successForData(statusMainModelService.saveOrUpdateData(list));
    }

    @PostMapping("statusModelChangeAdd")
    public Result<List<TcStatusDetailInstanceDto>> statusModelChangeAdd(@RequestBody StatusChangeAddDto statusChangeAddDto){
        return Result.successForData(statusMainModelService.statusModelChangeAdd(statusChangeAddDto));
    }

    @PostMapping("getStatusModelResult")
    public Result<StatusModelDataResult> getStatusModelResult(@RequestBody StatusModelQuery query){
        return Result.successForData(statusMainModelService.getStatusModelResult(query));
    }

}
