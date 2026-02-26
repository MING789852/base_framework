package com.xm.util.dingding.event;

import cn.hutool.json.JSONObject;

public interface DingDingEventHandler {
    String getEventName();
    void handle(JSONObject eventJson);
    //mq补偿专属操作
    void compensationHandle(JSONObject eventJson);
}
