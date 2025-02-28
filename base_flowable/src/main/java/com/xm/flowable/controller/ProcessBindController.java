package com.xm.flowable.controller;

import com.xm.core.params.Result;
import com.xm.flowable.domain.dto.SaveProcessBindData;
import com.xm.flowable.domain.vo.ProcessBindData;
import com.xm.flowable.service.ProcessBindService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("processBind")
@RequiredArgsConstructor
public class ProcessBindController {
    private final ProcessBindService processBindService;

    @PostMapping("saveProcessBindData")
    public Result<String> saveProcessBindData(@RequestBody SaveProcessBindData saveProcessBindData) {
        return Result.successForData(processBindService.saveProcessBindData(saveProcessBindData));
    }

    @GetMapping("getProcessBindData")
    public Result<List<ProcessBindData>> getProcessBindData(String groupName) {
        return Result.successForData(processBindService.getProcessBindData(groupName));
    }

    @GetMapping("getDictMapping")
    public Result<Map<String,Object>> getDictMapping() {
        return Result.successForData(processBindService.getDictMapping());
    }
}
