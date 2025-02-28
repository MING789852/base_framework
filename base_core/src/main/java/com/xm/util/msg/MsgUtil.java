package com.xm.util.msg;


import cn.hutool.core.collection.CollectionUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.msg.MsgService;
import com.xm.core.msg.params.Msg;
import com.xm.util.bean.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class MsgUtil {

    private final static Map<String, MsgService> msgServiceMap;

    static {
        msgServiceMap=SpringBeanUtil.getBeanMapOfType(MsgService.class);
        log.info("【消息】消息实现类->{}",msgServiceMap.keySet());
    }

    private static void valid(){
        if (CollectionUtil.isEmpty(msgServiceMap)){
            String msg="【消息】消息实现类为空";
            log.error(msg);
            throw new CommonException(msg);
        }
    }

    /**
     * 无视msgList里的消息类型，统一使用msgTypeList
     * @param msgTypeList MsgTypeConst类中定义
     */
    public static void createOrUpdateMsg(List<Msg> msgList,List<String> msgTypeList){
        valid();
        if (CollectionUtil.isEmpty(msgTypeList)){
            String msg="【消息】未选择消息类型";
            log.error(msg);
            throw new CommonException(msg);
        }
        for (String msgType:msgTypeList){
            MsgService msgService = msgServiceMap.get(msgType);
            if (msgService==null){
                log.error("【消息】未引入消息实现类->{}",msgType);
                continue;
            }
            msgService.createOrUpdateMsg(msgList);
        }
    }

    /**
     * 单独使用msgList
     * @param msgList 消息集合
     */
    public static void createOrUpdateMsg(List<Msg> msgList){
        valid();
        for (Msg msg:msgList){
            String msgType = msg.getType();
            if (msgType!=null){
                MsgService msgService = msgServiceMap.get(msgType);
                if (msgService==null){
                    log.error("【消息】未引入消息实现类->{}",msgType);
                    continue;
                }
                msgService.createOrUpdateMsg(msgList);
            }
        }
    }

    public static void deleteMsgList(String businessType, String businessKey){
        valid();
        msgServiceMap.forEach((key, value) -> {
            boolean flag = value.deleteMsgList(businessType, businessKey);
            log.info("【消息】删除消息->{},{},返回结果->{}",businessType,businessKey, flag);
        });
    }

    public static void deleteMsg(String businessType, String businessKey,String userId) {
        valid();
        msgServiceMap.forEach((key, value) -> {
            boolean flag = value.deleteMsg(businessType, businessKey,userId);
            log.info("【消息】删除消息->{},{},{},返回结果->{}",businessType,businessKey,userId, flag);
        });
    }

    public static void finishMsgList(String businessType, String businessKey){
        valid();
        msgServiceMap.forEach((key, value) -> {
            boolean flag = value.finishMsgList(businessType, businessKey);
            log.info("【消息】完成消息->{},{},返回结果->{}",businessType,businessKey, flag);
        });
    }

    public static void finishMsg(String businessType, String businessKey,String userId){
        valid();
        msgServiceMap.forEach((key, value) -> {
            boolean flag = value.finishMsg(businessType, businessKey,userId);
            log.info("【消息】完成消息->{},{},{},返回结果->{}",businessType,businessKey,userId, flag);
        });
    }
}
