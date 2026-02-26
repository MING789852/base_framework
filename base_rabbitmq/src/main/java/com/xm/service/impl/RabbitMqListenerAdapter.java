package com.xm.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.rabbitmq.RabbitMqActionListener;
import com.xm.core.rabbitmq.RabbitMqMsg;
import com.xm.core.rabbitmq.RabbitMqResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.retry.support.RetryTemplate;


@Slf4j
public class RabbitMqListenerAdapter extends MessageListenerAdapter {
    private final RabbitMqActionListener listener;
    private final boolean isDeadLetter;
    private final RetryTemplate retryTemplate;

    public RabbitMqListenerAdapter(RabbitMqActionListener listener, boolean isDeadLetter,RetryTemplate retryTemplate) {
        this.listener = listener;
        this.isDeadLetter = isDeadLetter;
        this.retryTemplate = retryTemplate;
    }

    private RabbitMqResult processMessage(Message message, Channel channel,RabbitMqMsg<?> cimforceSyncMsg) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        RabbitMqResult result;
        if (!isDeadLetter){
            result = listener.handler(cimforceSyncMsg);
        }else {
            result = listener.handlerDeadLetter(cimforceSyncMsg);
        }
        boolean handleResult = result.isHandleResult();
        if(handleResult){
            channel.basicAck(deliveryTag,false);
            return result;
        }else {
            //抛出异常执行重试
            //throw new CommonException("RabbitMq handleResult响应为false");
            //不抛出异常之前消费失败确认
            channel.basicNack(deliveryTag,false,isDeadLetter);
            return result;
        }
    }


    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        MessageConverter messageConverter = getMessageConverter();
        RabbitMqMsg<?> cimforceSyncMsg = (RabbitMqMsg<?>) messageConverter.fromMessage(message);
        try {
            if (retryTemplate != null){
                retryTemplate.execute(retryContext -> processMessage(message, channel, cimforceSyncMsg));
            }else {
                processMessage(message, channel, cimforceSyncMsg);
            }
        }catch (Exception e){
            log.error("【消息队列】MQ处理失败,数据->{},原因->{}", JSONUtil.toJsonStr(cimforceSyncMsg), ExceptionUtil.stacktraceToString(e));
            //死信队列执行失败，重新归队
            //非死信队列执行失败，直接丢弃
            channel.basicNack(deliveryTag,false,isDeadLetter);
        }
    }
}
