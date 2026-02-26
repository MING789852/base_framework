package com.xm.util.dingding.approval.requestRes;

import com.xm.util.dingding.approval.commonParams.Result;
import lombok.Data;

@Data
public class DDProcessInstanceRes {
	private Result result;
	private String success;

    private String requestId;
    private String code;
    private String message;

    private int httpStatus;
}