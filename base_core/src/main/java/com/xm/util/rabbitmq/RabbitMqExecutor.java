package com.xm.util.rabbitmq;

import com.xm.core.rabbitmq.RabbitMqActionListener;
import com.xm.core.rabbitmq.RabbitMqMsg;
import com.xm.core.rabbitmq.RabbitMqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DependsOn("SpringBeanUtil")
public class RabbitMqExecutor {
    private final RabbitMqService rabbitMqService;

    public RabbitMqExecutor(@Autowired(required = false) RabbitMqService rabbitMqService) {
        this.rabbitMqService = rabbitMqService;
    }

    public  void addDirectListener(String queueName, String exchangeName, String routingKey, RabbitMqActionListener listener){
        if (rabbitMqService==null){
            return;
        }
        rabbitMqService.addDirectListener(queueName, exchangeName, routingKey, listener);
    }

    public void deleteDirectListener(String queueName, String exchangeName, String routingKey){
        if (rabbitMqService==null){
            return;
        }
        rabbitMqService.deleteDirectListener(queueName, exchangeName, routingKey);
    }

    public void convertAndSend(String exchangeName, String routingKey, RabbitMqMsg<?> msg){
        if (rabbitMqService==null){
            return;
        }
        rabbitMqService.convertAndSend(exchangeName, routingKey, msg);
    }
}
