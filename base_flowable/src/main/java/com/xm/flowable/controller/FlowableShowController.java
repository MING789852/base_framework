package com.xm.flowable.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.core.params.Result;
import com.xm.flowable.domain.query.FlowableShowDataQuery;
import com.xm.flowable.domain.vo.FlowableShowDataVo;
import com.xm.flowable.domain.vo.TodoItemVo;
import com.xm.flowable.service.FlowableShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("flowableShow")
@RequiredArgsConstructor
public class FlowableShowController {
    private final FlowableShowService flowableShowService;

    @PostMapping("getData")
    public Result<Page<FlowableShowDataVo>> getData(@RequestBody FlowableShowDataQuery query) {
        return Result.successForData(flowableShowService.getData(query));
    }

    @PostMapping("getDictMapping")
    public Result<Map<String, Object>> getDictMapping() {
        return Result.successForData(flowableShowService.getDictMapping());
    }

     @PostMapping("getTodoList")
     public Result<List<TodoItemVo>> getTodoList() {
        return Result.successForData(flowableShowService.getTodoList());
    }
}
