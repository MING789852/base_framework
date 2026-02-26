package com.xm.core.rabbitmq;

import lombok.Data;

@Data
public class RabbitMqResult {
    private boolean handleResult;

    public static final RabbitMqResult SUCCESS = new RabbitMqResult(true);
    public static final RabbitMqResult FAIL = new RabbitMqResult(false);


    public RabbitMqResult(boolean handleResult) {
        this.handleResult = handleResult;
    }
}
