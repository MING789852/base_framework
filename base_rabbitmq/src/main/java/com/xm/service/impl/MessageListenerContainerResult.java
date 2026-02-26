package com.xm.service.impl;

import lombok.Data;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

@Data
public class MessageListenerContainerResult {
    private SimpleMessageListenerContainer handler;
    private SimpleMessageListenerContainer deadLetterHandler;
    private Queue xDeadLetterQueue;
    private Exchange xDeadLetterExchange;
    private Binding xxDeadLetterBinding;

    private Queue queue;
    private Exchange exchange;
    private Binding binding;
}
