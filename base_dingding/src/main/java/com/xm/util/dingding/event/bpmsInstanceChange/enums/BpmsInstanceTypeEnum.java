package com.xm.util.dingding.event.bpmsInstanceChange.enums;

/**
 * 实例状态变更类型：
 * start：审批实例开始
 * finish：审批正常结束（同意或拒绝）
 * terminate：审批终止（发起人撤销审批单）
 * delete：审批实例删除
 */
public enum BpmsInstanceTypeEnum {
    start,
    finish,
    terminate,
    delete
}
