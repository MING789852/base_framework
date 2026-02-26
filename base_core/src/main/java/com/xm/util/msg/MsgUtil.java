package com.xm.util.msg;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.msg.MsgResultHandleService;
import com.xm.core.msg.MsgService;
import com.xm.core.msg.params.Msg;
import com.xm.core.msg.params.MsgSendResult;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Slf4j
public class MsgUtil {

    private final static Map<String, MsgService> msgServiceMap;
    private final static Map<String, MsgResultHandleService> msgResultHandleService;

    static {
        msgServiceMap=SpringBeanUtil.getBeanMapOfType(MsgService.class);
        log.info("【消息】消息发送实现类->{}",msgServiceMap.keySet());
        msgResultHandleService = SpringBeanUtil.getBeanMapOfType(MsgResultHandleService.class);
        log.info("【消息】消息结果处理类->{}",msgResultHandleService.keySet());
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
        List<MsgSendResult> msgSendResultList=new ArrayList<>();
        for (String msgType:msgTypeList){
            MsgService msgService = msgServiceMap.get(msgType);
            if (msgService==null){
                log.error("【消息】未引入消息实现类->{}",msgType);
                continue;
            }
            //克隆消息对象(添加消息类型)之后传值
            List<Msg> copyList=new ArrayList<>();
            for (Msg msg:msgList){
                Msg copyMsg = BeanUtil.copyProperties(msg, Msg.class);
                copyMsg.setType(msgType);
                copyList.add(copyMsg);
            }
            MsgSendResult sendResult = msgService.createOrUpdateMsg(copyList);
            msgSendResultList.add(sendResult);
        }
        if (CollectionUtil.isNotEmpty(msgSendResultList)&&CollectionUtil.isNotEmpty(msgResultHandleService)){
            ThreadUtil.getExecutor().execute(() -> {
                msgResultHandleService.forEach((key,value)->{
                    value.execute(msgSendResultList);
                });
            });
        }
    }

    /**
     * 单独使用msgList
     * @param msgList 消息集合
     */
    public static void createOrUpdateMsg(List<Msg> msgList){
        valid();
        for (Msg msg:msgList){
            createOrUpdateMsg(Collections.singletonList(msg), Collections.singletonList(msg.getType()));
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

    public static void finishMsgByMsgType(String msgType,String businessType, String businessKey,String userId){
        valid();
        MsgService msgService = msgServiceMap.get(msgType);
        boolean flag = msgService.finishMsg(businessType, businessKey,userId);
        log.info("【消息】完成消息->{},{},{},返回结果->{}",businessType,businessKey,userId, flag);
    }
}
