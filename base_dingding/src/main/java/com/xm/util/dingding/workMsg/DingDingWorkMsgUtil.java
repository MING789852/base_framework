package com.xm.util.dingding.workMsg;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.configuration.dingding.DingdingProperty;
import com.xm.util.dingding.DingDingUtil;
import com.xm.util.dingding.workMsg.requestParams.DDWorkMsgParam;
import com.xm.util.dingding.workMsg.requestRes.DDWorkMsgRes;
import com.xm.util.http.HttpUtils;
import com.xm.util.http.enums.HttpMethodEnum;

public class DingDingWorkMsgUtil {

    /**
     * 发送工作通知
     */
    public static DDWorkMsgRes sendMsg(DDWorkMsgParam param){
        DingdingProperty dingdingProperty = DingDingUtil.getCurrentDingdingProperty();
        param.setAgent_id(dingdingProperty.getAgentId());
        String url = StrUtil.format("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2?access_token={}", DingDingUtil.getAccessToken());
        String resStr= HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, null, JSONUtil.toJsonStr(param));
        DDWorkMsgRes res=JSONUtil.toBean(resStr, DDWorkMsgRes.class);
        int errcode=res.getErrcode();
        if (errcode==0){
            return res;
        }else {
            throw new CommonException(StrUtil.format("【发送钉钉消息失败】{}",res));
        }
    }
}
