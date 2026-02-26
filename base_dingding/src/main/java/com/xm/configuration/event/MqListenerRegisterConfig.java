package com.xm.configuration.event;

import com.xm.util.dingding.event.enums.DDEventRabbitMqEnum;
import com.xm.util.dingding.event.listener.DDEventRabbitMqActionListener;
import com.xm.util.rabbitmq.RabbitMqExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class MqListenerRegisterConfig {

    private final RabbitMqExecutor rabbitMqExecutor;

    @PostConstruct
    public void init() {
        rabbitMqExecutor.addDirectListener
                (DDEventRabbitMqEnum.dingding_event_queue.name(), DDEventRabbitMqEnum.dingding_event_exchange.name(),
                        DDEventRabbitMqEnum.dingding_event_routing_key.name(), new DDEventRabbitMqActionListener());
    }
}
