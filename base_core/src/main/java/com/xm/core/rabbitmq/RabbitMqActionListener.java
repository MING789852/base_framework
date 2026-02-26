package com.xm.core.rabbitmq;

public interface RabbitMqActionListener {
    RabbitMqResult  handler(RabbitMqMsg<?> msg);
    RabbitMqResult  handlerDeadLetter(RabbitMqMsg<?> msg);
}
