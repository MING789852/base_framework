package com.xm.status.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.module.core.params.QueryData;
import com.xm.core.params.Result;
import com.xm.status.domain.entity.TcStatusDetailModel;
import com.xm.status.service.TcStatusDetailModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("statusDetailModel")
public class StatusDetailModelController {

    private final TcStatusDetailModelService statusDetailModelService;

    @PostMapping("selectByPage")
    public Result<Page<TcStatusDetailModel>> selectByPage(@RequestBody QueryData<TcStatusDetailModel> queryData){
        return Result.successForData(statusDetailModelService.selectByPage(queryData));
    }

    @PostMapping("deleteData")
    public Result<String> deleteData(@RequestBody List<TcStatusDetailModel> list){
        return Result.successForData(statusDetailModelService.deleteData(list));
    }

    @PostMapping("saveOrUpdateData")
    public Result<String> saveOrUpdateData(@RequestBody List<TcStatusDetailModel> list){
        return Result.successForData(statusDetailModelService.saveOrUpdateData(list));
    }

    @GetMapping("getDictMapping")
    public Result<Map<String, Object>> getDictMapping(){
        return Result.successForData(statusDetailModelService.getDictMapping());
    }

    @GetMapping("getDetailModelMappingByMainModelId")
    public Result<Map<String, String>> getDetailModelMappingByMainModelId(String mainModelId){
        return Result.successForData(statusDetailModelService.getDetailModelMappingByMainModelId(mainModelId));
    }
}
