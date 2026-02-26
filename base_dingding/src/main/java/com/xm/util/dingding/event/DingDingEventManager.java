package com.xm.util.dingding.event;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.xm.advice.exception.exception.CommonException;
import com.xm.util.dingding.event.enums.DingDingEventControllerActionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DingDingEventManager {
    private static final ConcurrentHashMap<String,DingDingEventHandler> dingDingEventHandlerMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String,RequestMappingInfo> requestMappingInfoMap = new ConcurrentHashMap<>();

    public static void registerEvent(DingDingEventHandler dingDingEventHandler) {
        String eventName = dingDingEventHandler.getEventName();
        if (dingDingEventHandlerMap.containsKey(eventName)){
            String errorMsg = StrUtil.format("eventName->{}已存在", eventName);
            throw new CommonException(errorMsg);
        }
        dingDingEventHandlerMap.put(eventName, dingDingEventHandler);
        log.info("注册钉钉事件成功->{}", eventName);
    }

    public static void handleEvent(String eventName, JSONObject eventJson) {
        DingDingEventHandler dingDingEventHandler = dingDingEventHandlerMap.get(eventName);
        if (dingDingEventHandler != null) {
            dingDingEventHandler.handle(eventJson);
        }
    }

    public static void compensateHandleEvent(String eventName, JSONObject eventJson) {
        DingDingEventHandler dingDingEventHandler = dingDingEventHandlerMap.get(eventName);
        if (dingDingEventHandler != null) {
            dingDingEventHandler.compensationHandle(eventJson);
        }
    }

    public static void registerEventController(RequestMappingHandlerMapping requestMappingHandlerMapping,
                                               String urlPath,String token, String aesKey, String corpId,
                                               DingDingEventControllerActionType actionType){
        try {
            if (requestMappingHandlerMapping == null){
                throw new CommonException("RequestMappingHandlerMapping不能为空");
            }
            if (StrUtil.isBlank(urlPath)){
                throw new CommonException("urlPath不能为空");
            }
            if (requestMappingInfoMap.containsKey(urlPath)){
                String errorMsg = StrUtil.format("urlPath->{}已存在", urlPath);
                throw new CommonException(errorMsg);
            }
            if (StrUtil.isBlank(token)){
                throw new CommonException("token不能为空");
            }
            if (StrUtil.isBlank(aesKey)){
                throw new CommonException("aesKey不能为空");
            }
            if (StrUtil.isBlank(corpId)){
                throw new CommonException("corpId不能为空");
            }
            if (actionType == null){
                throw new CommonException("actionType不能为空");
            }
            Class<DingDingEventController> dingDingEventControllerClass = DingDingEventController.class;
            DingDingEventController dingDingEventController = new DingDingEventController(token, aesKey, corpId,actionType);

            // 获取接口方法
            Method method = dingDingEventControllerClass.getMethod(
                    "eventCallBack",
                    String.class, String.class, String.class, JSONObject.class);

            // 创建RequestMapping信息
            RequestMappingInfo.BuilderConfiguration builderConfiguration = requestMappingHandlerMapping.getBuilderConfiguration();
            RequestMappingInfo mappingInfo = RequestMappingInfo
                    .paths(urlPath)
                    .options(builderConfiguration)
                    .methods(RequestMethod.POST)
                    .build();
            // 注册映射
            requestMappingHandlerMapping.registerMapping(mappingInfo, dingDingEventController, method);
            // 保存映射信息
            requestMappingInfoMap.put(urlPath,mappingInfo);
            log.info("注册钉钉事件controller成功->{}", urlPath);
        }catch (Exception e){
            String errorMsg = StrUtil.format("注册钉钉事件controller失败->{}", ExceptionUtil.stacktraceToString(e));
            log.error(errorMsg);
            throw new CommonException(errorMsg);
        }
    }

    public static void unRegisterEventController(RequestMappingHandlerMapping requestMappingHandlerMapping,
                                                 String urlPath){
        if (requestMappingHandlerMapping == null){
            throw new CommonException("RequestMappingHandlerMapping不能为空");
        }
        if (StrUtil.isBlank(urlPath)){
            throw new CommonException("urlPath不能为空");
        }
        RequestMappingInfo requestMappingInfo = requestMappingInfoMap.get(urlPath);
        if (requestMappingInfo != null){
            requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
            requestMappingInfoMap.remove(urlPath);
            log.info("注销钉钉事件controller成功->{}", urlPath);
        }
    }
}
