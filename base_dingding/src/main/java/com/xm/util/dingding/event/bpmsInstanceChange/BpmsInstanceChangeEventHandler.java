package com.xm.util.dingding.event.bpmsInstanceChange;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xm.util.dingding.event.DingDingEventHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class BpmsInstanceChangeEventHandler implements DingDingEventHandler {

    private final Map<String, ProcessCodeHandler> processCodeHandlerMap;

    public BpmsInstanceChangeEventHandler(List<ProcessCodeHandler> processCodeHandlers) {
        if (CollectionUtil.isEmpty(processCodeHandlers)){
            processCodeHandlerMap = new ConcurrentHashMap<>();
        }else {
            processCodeHandlerMap = processCodeHandlers.stream().collect(Collectors.groupingBy(ProcessCodeHandler::getProcessCode,
                    Collectors.collectingAndThen(Collectors.toList(),item->item.get(0))));
        }
    }

    @Override
    public String getEventName() {
        return "bpms_instance_change";
    }

    @Override
    public void handle(JSONObject eventJson) {
        BpmsInstanceChangeEvent bean = JSONUtil.toBean(eventJson, BpmsInstanceChangeEvent.class);
        String processCode = bean.getProcessCode();
        ProcessCodeHandler processCodeHandler = processCodeHandlerMap.get(processCode);
        if (processCodeHandler != null){
            log.info("【钉钉流程变更】【{}】流程->{}变更",getEventName(),processCodeHandler.getProcessName());
            processCodeHandler.handle(bean);
        }
    }

    @Override
    public void compensationHandle(JSONObject eventJson) {
        //todo 补偿操作
        log.info("【钉钉流程变更】补偿操作->{}",eventJson);
    }
}
