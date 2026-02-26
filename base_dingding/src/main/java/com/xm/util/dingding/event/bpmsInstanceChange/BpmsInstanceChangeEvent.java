package com.xm.util.dingding.event.bpmsInstanceChange;

import lombok.Data;

@Data
public class BpmsInstanceChangeEvent {
	private String eventId;
	private String processInstanceId;
	private long finishTime;
	private String corpId;
	private String eventType;
	private String businessId;
    //类型取值BpmsInstanceTypeEnum
	private String type;
	private String title;
	private String url;
    //取值BpmsInstanceResultEnum
	private String result;
	private long createTime;
	private long eventTime;
	private String processCode;
	private String bizCategoryId;
	private String businessType;
	private String bizId;
	private String staffId;
}