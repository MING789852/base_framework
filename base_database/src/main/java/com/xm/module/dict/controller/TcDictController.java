package com.xm.module.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.module.core.params.QueryData;
import com.xm.core.params.Result;
import com.xm.module.dict.domain.dto.SaveOrUpdateDictDto;
import com.xm.module.dict.domain.entity.TcDict;
import com.xm.module.dict.domain.query.DictQuery;
import com.xm.module.dict.service.TcDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("dict")
public class TcDictController {

    private final TcDictService dictService;

    @PostMapping("selectByPage")
    public Result<Page<TcDict>> selectByPage(@RequestBody QueryData queryData){
        return Result.successForData(dictService.selectByPage(queryData));
    }

    @PostMapping("deleteData")
    public Result<String> deleteData(@RequestBody List<TcDict> list){
        return Result.successForData(dictService.deleteData(list));
    }

    @PostMapping("saveOrUpdateData")
    public Result<String> saveOrUpdateData(@RequestBody SaveOrUpdateDictDto dto){
        return Result.successForData(dictService.saveOrUpdateData(dto));
    }

    @GetMapping("getDictByGroupKey")
    public Result<Map<String, String>> getDictByGroupKey(String groupKey){
        return Result.successForData(dictService.getDictByGroupKey(groupKey));
    }

    @PostMapping("getDictMappingByQuery")
    public Result<Map<String,Object>> getDictMappingByQuery(@RequestBody List<DictQuery> dictQueryList){
        return Result.successForData(dictService.getDictMappingByQuery(dictQueryList));
    }
}