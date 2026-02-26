package com.xm.util.dingding.approval.commonParams;

import lombok.Data;

@Data
public class TasksItem{
	private String result;
	private String processInstanceId;
	private String activityId;
	private String finishTime;
	private String pcUrl;
	private String createTime;
	private String mobileUrl;
	private String userId;
	private int taskId;
	private String status;
}