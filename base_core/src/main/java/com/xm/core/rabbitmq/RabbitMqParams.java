package com.xm.core.rabbitmq;

import com.xm.util.retry.params.RetryParams;
import lombok.Data;

@Data
public class RabbitMqParams {
    private RetryParams handlerRetryParams;
    private RetryParams deadLetterRetryParams;
}
