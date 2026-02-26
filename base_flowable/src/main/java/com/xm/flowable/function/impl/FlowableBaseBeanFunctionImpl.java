package com.xm.flowable.function.impl;

import cn.hutool.json.JSONUtil;
import com.xm.flowable.function.FlowableBeanFunction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("baseBeanFunction")
public class FlowableBaseBeanFunctionImpl implements FlowableBeanFunction {

    public List<Object> parseJsonToList(String json){
        return JSONUtil.toList(json, Object.class);
    }
}
