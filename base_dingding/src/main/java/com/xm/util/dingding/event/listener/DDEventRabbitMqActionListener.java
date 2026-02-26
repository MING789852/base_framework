package com.xm.util.dingding.event.listener;

import cn.hutool.json.JSONObject;
import com.xm.core.rabbitmq.RabbitMqActionListener;
import com.xm.core.rabbitmq.RabbitMqMsg;
import com.xm.core.rabbitmq.RabbitMqResult;
import com.xm.util.dingding.event.DingDingEventManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DDEventRabbitMqActionListener implements RabbitMqActionListener {
    @Override
    public RabbitMqResult handler(RabbitMqMsg<?> msg) {
        try {
            Object data = msg.getData();
            if (data instanceof JSONObject){
                JSONObject eventJson = (JSONObject) data;
                String eventType = eventJson.getStr("EventType");
                log.info("【钉钉事件订阅】【异步消息队列】处理操作：{}事件", eventType);
                DingDingEventManager.handleEvent(eventType, eventJson);
            }
            return RabbitMqResult.SUCCESS;
        }catch (Exception e){
            log.error("【钉钉事件订阅】处理操作异常", e);
            return RabbitMqResult.FAIL;
        }
    }

    @Override
    public RabbitMqResult handlerDeadLetter(RabbitMqMsg<?> msg) {
        try {
            Object data = msg.getData();
            if (data instanceof JSONObject){
                JSONObject eventJson = (JSONObject) data;
                String eventType = eventJson.getStr("EventType");
                log.info("【钉钉事件订阅】【异步消息队列】补偿操作:{}事件", eventType);
                DingDingEventManager.compensateHandleEvent(eventType, eventJson);
            }
            return RabbitMqResult.SUCCESS;
        }catch (Exception e){
            log.error("【钉钉事件订阅】补偿操作异常", e);
            return RabbitMqResult.FAIL;
        }
    }
}
