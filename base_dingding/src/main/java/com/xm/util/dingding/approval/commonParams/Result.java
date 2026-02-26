package com.xm.util.dingding.approval.commonParams;

import java.util.List;
import lombok.Data;

@Data
public class Result{
	private String mainProcessInstanceId;
	private String finishTime;
	private List<String> attachedProcessInstanceIds;
	private String businessId;
	private String title;
	private String originatorDeptId;
	private List<OperationRecordsItem> operationRecords;
	private List<FormComponentValuesItem> formComponentValues;
	private String result;
	private List<String> ccUserIds;
	private String bizAction;
	private String createTime;
	private String originatorUserId;
	private String bizData;
	private List<String> approverUserIds;
	private List<TasksItem> tasks;
	private String originatorDeptName;
	private String status;
}