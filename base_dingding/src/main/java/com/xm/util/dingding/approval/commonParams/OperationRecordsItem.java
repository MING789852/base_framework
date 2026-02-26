package com.xm.util.dingding.approval.commonParams;

import java.util.List;
import lombok.Data;

@Data
public class OperationRecordsItem{
	private String date;
	private String result;
	private String activityId;
	private List<String> images;
	private List<AttachmentsItem> attachments;
	private List<String> ccUserIds;
	private String showName;
	private String remark;
	private String type;
	private String userId;
}