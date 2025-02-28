package com.xm.configuration.rabbitMq;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMqConfig {

    public final static String rabbitmq_template="rabbitmq_template";


    @Bean(rabbitmq_template)
    public RabbitTemplate rabbitTemplate(@Autowired ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate=new RabbitTemplate(connectionFactory);
        rabbitTemplate.setConfirmCallback((correlationData, ack, s) -> {
            // ack判断消息发送到交换机是否成功
            if (!ack) {
                log.error("消息到达交换机失败,失败原因->{}",s);
            }
        });

        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            /**
             * 消息路由失败，回调
             * 消息(带有路由键routingKey)到达交换机，与交换机的所有绑定键进行匹配，匹配不到触发回调
             */
            @Override
            public void returnedMessage(@NonNull ReturnedMessage returnedMessage) {
                log.info("交换机无法投递队列,失败原因->{}",returnedMessage);
            }
        });

        //1、 丢弃消息（默认）即 rabbitTemplate.setMandatory(false);
        //2、 返回给消息发送方 ReturnCallBack 即 rabbitTemplate.setMandatory(true);
        rabbitTemplate.setMandatory(true);

        return rabbitTemplate;
    }
}
