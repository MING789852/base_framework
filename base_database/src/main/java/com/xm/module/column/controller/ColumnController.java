package com.xm.module.column.controller;

import com.xm.annotation.IgnoreAuth;
import com.xm.module.column.domain.params.ColumnConfig;
import com.xm.core.params.ColumnProps;
import com.xm.module.column.service.ColumnService;
import com.xm.core.params.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("column")
@IgnoreAuth
public class ColumnController {

    private final ColumnService columnService;

    @PostMapping("getColumnPropsConfig")
    public Result<List<ColumnProps>> getColumnPropsConfig(@RequestBody ColumnConfig columnConfig){
        return Result.successForData(columnService.getColumnPropsConfig(columnConfig));
    }


    @PostMapping("saveColumnPropsConfig")
    public Result<String> saveColumnPropsConfig(@RequestBody ColumnConfig columnConfig){
        return Result.successForData(columnService.saveColumnPropsConfig(columnConfig));
    }

    @PostMapping("resetColumnPropsConfig")
    public Result<String> resetColumnPropsConfig(@RequestBody ColumnConfig columnConfig){
        return Result.successForData(columnService.resetColumnPropsConfig(columnConfig));
    }
}
