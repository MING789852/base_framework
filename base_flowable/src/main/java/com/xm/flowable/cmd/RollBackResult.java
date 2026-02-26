package com.xm.flowable.cmd;

import lombok.Data;
import org.flowable.bpmn.model.FlowNode;

import java.util.List;
import java.util.Map;

@Data
public class RollBackResult {
    //回滚之后流程是否结束
    private boolean finish;
    //流程未结束时，该属性有效
    //解决代办转发之后回滚流程，执行人未改变
    private Map<String,String> actIdAndAssigneeMapping;
}
