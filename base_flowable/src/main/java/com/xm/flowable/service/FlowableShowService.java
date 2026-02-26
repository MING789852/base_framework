package com.xm.flowable.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.flowable.domain.query.FlowableShowDataQuery;
import com.xm.flowable.domain.vo.FlowableShowDataVo;
import com.xm.flowable.domain.vo.TodoItemVo;

import java.util.List;
import java.util.Map;

public interface FlowableShowService {
    Page<FlowableShowDataVo> getData(FlowableShowDataQuery query);
    Map<String,Object> getDictMapping();
    List<TodoItemVo> getTodoList();
}
