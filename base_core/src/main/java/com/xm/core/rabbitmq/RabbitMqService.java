package com.xm.core.rabbitmq;

public interface RabbitMqService{
    void deleteDirectListener(String queueName, String exchangeName, String routingKey);
    void addDirectListener(String queueName, String exchangeName, String routingKey, RabbitMqActionListener listener);
    void addDirectListener(String queueName, String exchangeName, String routingKey, RabbitMqActionListener listener,RabbitMqParams  params);
    void convertAndSend(String exchangeName, String routingKey, RabbitMqMsg<?> msg);
}
