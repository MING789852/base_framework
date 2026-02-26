package com.xm.util.dingding.event;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xm.annotation.IgnoreAuth;
import com.xm.core.rabbitmq.RabbitMqMsg;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.dingding.event.enums.DDEventRabbitMqEnum;
import com.xm.util.dingding.event.enums.DingDingEventControllerActionType;
import com.xm.util.rabbitmq.RabbitMqExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Slf4j
public class DingDingEventController {

    private final String token;
    private final String aesKey;
    private final String corpId;
    private final DingDingEventControllerActionType actionType;

    public DingDingEventController(String token, String aesKey, String corpId, DingDingEventControllerActionType actionType) {
        this.token = token;
        this.aesKey = aesKey;
        this.corpId = corpId;
        this.actionType = actionType;
    }

    private ThreadPoolTaskExecutor executor;


    @IgnoreAuth
    @ResponseBody
    public Map<String, String> eventCallBack(
            @RequestParam(value = "msg_signature", required = false) String msg_signature,
            @RequestParam(value = "timestamp", required = false) String timeStamp,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestBody(required = false) JSONObject json){
        try {

            // 1. 从http请求中获取加解密参数
            // 2. 使用加解密类型
            // 2、调用订阅事件接口订阅的事件为企业级事件推送，此时OWNER_KEY为：开发者后台应用的Client ID（原企业内部应用 appKey )
            DingDingCallbackCrypto callbackCrypto = new DingDingCallbackCrypto(token, aesKey, corpId);
            String encryptMsg = json.getStr("encrypt");
            String decryptMsg = callbackCrypto.getDecryptMsg(msg_signature, timeStamp, nonce, encryptMsg);

            // 3. 反序列化回调事件json数据
            JSONObject eventJson = JSONUtil.parseObj(decryptMsg);
            String eventType = eventJson.getStr("EventType");

            // 4. 根据EventType分类处理
            if ("check_url".equals(eventType)) {
                // 测试回调url的正确性
                log.info("【钉钉事件订阅】测试回调url的正确性");
            }else {
                if (actionType == DingDingEventControllerActionType.sync){
                    log.info("【钉钉事件订阅】【同步操作】发生了：{}事件", eventType);
                    DingDingEventManager.handleEvent(eventType, eventJson);
                }else if (actionType == DingDingEventControllerActionType.thread){
                    log.info("【钉钉事件订阅】【异步线程池】发生了：{}事件", eventType);
                    if (executor==null){
                        executor = SpringBeanUtil.getBeanByClass(ThreadPoolTaskExecutor.class);
                    }
                    executor.execute(() -> DingDingEventManager.handleEvent(eventType, eventJson));
                }else if (actionType == DingDingEventControllerActionType.rabbitmq){
                    RabbitMqMsg<JSONObject> msg = new RabbitMqMsg<>(eventJson);
                    RabbitMqExecutor executor = SpringBeanUtil.getBeanByClass(RabbitMqExecutor.class);
                    executor.convertAndSend(DDEventRabbitMqEnum.dingding_event_exchange.name(),
                            DDEventRabbitMqEnum.dingding_event_routing_key.name(), msg);
                }
            }

            // 5. 返回success的加密数据
            return callbackCrypto.getEncryptedMap("success");
        }catch (Exception e){
            log.error("【钉钉事件订阅】异常", e);
        }
        return null;
    }
}
