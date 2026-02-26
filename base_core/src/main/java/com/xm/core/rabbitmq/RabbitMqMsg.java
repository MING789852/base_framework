package com.xm.core.rabbitmq;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class RabbitMqMsg <T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private T data;

    public RabbitMqMsg(String id, T data) {
        this.id = id;
        this.data = data;
    }

    public RabbitMqMsg(T data) {
        this.data = data;
    }
}