package com.xm.flowable.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class ElementProp {
    //节点属性
    private String assignee;
    private String assigneeName;
    //userTask节点类型： single单签(默认)、 all会签、 or或签
    //serviceTask节点类型：cc抄送(默认)
    //content1
    private String userTaskApprovalType;
    //候选人员 content2
    private List<String> candidateUsers;
    //审批通过数量 content3
    private Integer approvalCount;
    //业务标签 content4
    private String category;

    //自定义变量
    //content5
    //fixedSelect 固定人员选择
    //customVar 自定义变量
    private String userConfigActionType;
    //content6
    private String customVar;

    //边属性存储到content1
    private String conditionalExpression;
}
