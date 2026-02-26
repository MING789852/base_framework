package com.xm.util.dingding.approval;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.util.dingding.DingDingUtil;
import com.xm.util.dingding.approval.requestRes.DDProcessInstanceRes;
import com.xm.util.http.HttpUtils;
import com.xm.util.http.enums.HttpMethodEnum;
import com.xm.util.http.params.HttpOperationResponse;

import java.util.HashMap;
import java.util.Map;

public class DingDingApprovalUtil {
    /**
     * 钉钉审批详情
     * 参考：<a href="https://open.dingtalk.com/document/orgapp/obtains-the-details-of-a-single-approval-instance-pop">...</a>
     */
    public static DDProcessInstanceRes getProcessInstanceDetail(String processId){
        String url = StrUtil.format("https://api.dingtalk.com/v1.0/workflow/processInstances?processInstanceId={}",processId);
        Map<String,String> header=new HashMap<>();
        header.put("x-acs-dingtalk-access-token", DingDingUtil.getAccessToken());
        HttpOperationResponse res= HttpUtils.doJsonRequestReturnResponse(url, HttpMethodEnum.GET,header, null);
        DDProcessInstanceRes resStr=JSONUtil.toBean(res.getStrContent(), DDProcessInstanceRes.class);
        int httpStatus = res.getHttpStatus();
        resStr.setHttpStatus(httpStatus);
        resStr.setSuccess(httpStatus==200?"true":"false");
        return resStr;
    }
}
