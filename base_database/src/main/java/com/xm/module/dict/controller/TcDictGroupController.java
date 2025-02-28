package com.xm.module.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.module.core.params.QueryData;
import com.xm.core.params.Result;
import com.xm.module.dict.domain.entity.TcDictGroup;
import com.xm.module.dict.service.TcDictGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("dictGroup")
public class TcDictGroupController {

    private final TcDictGroupService dictGroupService;

    @PostMapping("selectByPage")
    public Result<Page<TcDictGroup>> selectByPage(@RequestBody QueryData queryData){
        return Result.successForData(dictGroupService.selectByPage(queryData));
    }

    @PostMapping("deleteData")
    public Result<String> deleteData(@RequestBody List<TcDictGroup> list){
        return Result.successForData(dictGroupService.deleteData(list));
    }

    @PostMapping("saveOrUpdateData")
    public Result<String> saveOrUpdateData(@RequestBody List<TcDictGroup> list){
        return Result.successForData(dictGroupService.saveOrUpdateData(list));
    }
}
