package com.xm.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.rabbitmq.*;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.lock.LockUtil;
import com.xm.util.retry.RetryUtil;
import com.xm.util.retry.params.RetryParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqServiceImpl implements RabbitMqService {

    private final RabbitAdmin rabbitAdmin;

    private final ConnectionFactory connectionFactory;


    private final Map<String, MessageListenerContainerResult> listenerContainerMap=new ConcurrentHashMap<>();

    @Override
    public void deleteDirectListener(String queueName, String exchangeName, String routingKey) {
        String key= StrUtil.format("addDirectListener:{}#{}#{}",exchangeName,queueName,routingKey);
        LockUtil.lock(key, ()->{
            MessageListenerContainerResult result = listenerContainerMap.get(key);
            if (result!=null){
                result.getHandler().stop();
                result.getDeadLetterHandler().stop();

                Exchange exchange = result.getExchange();
                Queue queue = result.getQueue();
                Binding binding = result.getBinding();

                rabbitAdmin.deleteQueue(queue.getName());
                rabbitAdmin.deleteExchange(exchange.getName());
                rabbitAdmin.removeBinding(binding);

                Queue xDeadLetterQueue = result.getXDeadLetterQueue();
                Exchange xDeadLetterExchange = result.getXDeadLetterExchange();
                Binding xxDeadLetterBinding = result.getXxDeadLetterBinding();

                rabbitAdmin.deleteQueue(xDeadLetterQueue.getName());
                rabbitAdmin.deleteExchange(xDeadLetterExchange.getName());
                rabbitAdmin.removeBinding(xxDeadLetterBinding);

                listenerContainerMap.remove(key);
            }
            return null;
        }, (e) -> {
            throw new CommonException(e.getMessage());
        });
    }

    @Override
    public void addDirectListener(String queueName, String exchangeName, String routingKey, RabbitMqActionListener listener, RabbitMqParams params) {
        if (params==null){
            throw new CommonException("params不能为空");
        }
        String key= StrUtil.format("addDirectListener:{}#{}#{}",exchangeName,queueName,routingKey);
        LockUtil.lock(key, ()->{
            MessageListenerContainerResult result = listenerContainerMap.get(key);
            if (result==null){
                String xDeadLetterExchangeName="xDeadLetter_"+exchangeName;
                String xDeadLetterQueueName="xDeadLetter_"+queueName;
                String xDeadLetterRoutingKey="xDeadLetter_"+routingKey;
                Queue xDeadLetterQueue = new Queue(xDeadLetterQueueName, true, false, false);
                DirectExchange xDeadLetterExchange = new DirectExchange(xDeadLetterExchangeName,true,false);
                Binding xxDeadLetterBinding = BindingBuilder.bind(xDeadLetterQueue).to(xDeadLetterExchange).with(xDeadLetterRoutingKey);
                try {
                    rabbitAdmin.declareQueue(xDeadLetterQueue);
                }catch (Exception e){
                    log.error("【消息队列】queue->{}已存在",xDeadLetterQueue);
                }
                try {
                    rabbitAdmin.declareExchange(xDeadLetterExchange);
                }catch (Exception e){
                    log.error("【消息队列】exchange->{}已存在",xDeadLetterExchange);
                }
                try {
                    rabbitAdmin.declareBinding(xxDeadLetterBinding);
                }catch (Exception e){
                    log.error("【消息队列】binding routingKey->{}已存在",xxDeadLetterBinding);
                }

                Map<String, Object> arguments=new HashMap<>();
                arguments.put("x-dead-letter-exchange", xDeadLetterExchangeName);
                arguments.put("x-dead-letter-routing-key", xDeadLetterRoutingKey);
                Queue queue = new Queue(queueName, true, false, false,arguments);
                DirectExchange exchange = new DirectExchange(exchangeName, true, false);
                Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
                try {
                    rabbitAdmin.declareQueue(queue);
                }catch (Exception e){
                    log.error("【消息队列】queue->{}已存在",queueName);
                }
                try {
                    rabbitAdmin.declareExchange(exchange);
                }catch (Exception e){
                    log.error("【消息队列】exchange->{}已存在",exchangeName);
                }
                try {
                    rabbitAdmin.declareBinding(binding);
                }catch (Exception e){
                    log.error("【消息队列】binding routingKey->{}已存在",routingKey);
                }

                MessageConverter messageConverter = rabbitAdmin.getRabbitTemplate().getMessageConverter();

                SimpleMessageListenerContainer handler = new SimpleMessageListenerContainer();
                handler.setConnectionFactory(connectionFactory);
                handler.setQueueNames(queueName);
                RetryTemplate handleRetryTemplate=null;
                if (params.getHandlerRetryParams()!=null){
                    handleRetryTemplate= RetryUtil.getRetryTemplate(params.getHandlerRetryParams(),"Rabbitmq处理重试");
                }
                RabbitMqListenerAdapter handlerMessageListener=new RabbitMqListenerAdapter(listener,false,handleRetryTemplate);
                handlerMessageListener.setMessageConverter(messageConverter);
                handler.setMessageListener(handlerMessageListener);
                handler.setConcurrentConsumers(1);
                handler.setMaxConcurrentConsumers(5);
                handler.setPrefetchCount(100);
                handler.setAcknowledgeMode(AcknowledgeMode.MANUAL);
                //声明失败重试
                handler.setDeclarationRetries(2);
                handler.setRetryDeclarationInterval(5000L);
                handler.start();

                SimpleMessageListenerContainer deadLetterHandler = new SimpleMessageListenerContainer();
                deadLetterHandler.setConnectionFactory(connectionFactory);
                deadLetterHandler.setQueueNames(xDeadLetterQueueName);
                RetryTemplate deadLetterRetryTemplate=null;
                if (params.getDeadLetterRetryParams()!=null){
                    deadLetterRetryTemplate=RetryUtil.getRetryTemplate(params.getDeadLetterRetryParams(),"Rabbitmq处理失败重试");
                }
                RabbitMqListenerAdapter deadLetterHandlerMessageListener=new RabbitMqListenerAdapter(listener,true,deadLetterRetryTemplate);
                deadLetterHandlerMessageListener.setMessageConverter(messageConverter);
                deadLetterHandler.setMessageListener(deadLetterHandlerMessageListener);
                deadLetterHandler.setConcurrentConsumers(1);
                deadLetterHandler.setMaxConcurrentConsumers(5);
                deadLetterHandler.setPrefetchCount(100);
                deadLetterHandler.setAcknowledgeMode(AcknowledgeMode.MANUAL);
                deadLetterHandler.setDefaultRequeueRejected(false);
                //声明失败重试
                deadLetterHandler.setDeclarationRetries(2);
                deadLetterHandler.setRetryDeclarationInterval(5000L);
                deadLetterHandler.start();


                Map<String,Object> linkerHashMap=new LinkedHashMap<>();
                linkerHashMap.put("queueName",queueName);
                linkerHashMap.put("exchangeName",exchangeName);
                linkerHashMap.put("routingKey",routingKey);
                linkerHashMap.put("xDeadLetterExchangeName",xDeadLetterExchangeName);
                linkerHashMap.put("xDeadLetterQueueName",xDeadLetterQueueName);
                linkerHashMap.put("xDeadLetterRoutingKey",xDeadLetterRoutingKey);
                log.info("【注册消息队列】成功->{}",linkerHashMap);


                MessageListenerContainerResult container=new MessageListenerContainerResult();
                container.setHandler(handler);
                container.setDeadLetterHandler(deadLetterHandler);
                container.setXDeadLetterExchange(xDeadLetterExchange);
                container.setXDeadLetterQueue(xDeadLetterQueue);
                container.setXxDeadLetterBinding(xxDeadLetterBinding);
                container.setBinding(binding);
                container.setQueue(queue);
                container.setExchange(exchange);
                listenerContainerMap.put(key, container);
            }
            return "";
        }, (e) -> {
            throw new CommonException(ExceptionUtil.stacktraceToString(e));
        });
    }

    @Override
    public void addDirectListener(String queueName, String exchangeName, String routingKey, RabbitMqActionListener listener) {
        RabbitMqParams params=new RabbitMqParams();
        params.setHandlerRetryParams(new RetryParams());
        addDirectListener(queueName, exchangeName, routingKey, listener,params);
    }

    @Override
    public void convertAndSend(String exchangeName, String routingKey, RabbitMqMsg<?> msg) {
        String id = msg.getId();
        if (StrUtil.isBlank(id)){
            id= SnowIdUtil.getSnowId();
        }
        CorrelationData correlationData=new CorrelationData(id);
        rabbitAdmin.getRabbitTemplate().convertAndSend(exchangeName, routingKey, msg,correlationData);
    }
}
