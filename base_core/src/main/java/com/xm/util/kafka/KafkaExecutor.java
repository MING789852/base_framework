package com.xm.util.kafka;

import com.xm.core.kafka.KafkaConsumersInfo;
import com.xm.core.kafka.KafkaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@DependsOn("SpringBeanUtil")
public class KafkaExecutor {
    private final KafkaService kafkaService;

    public KafkaExecutor(@Autowired(required = false) KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    public  List<KafkaConsumersInfo> getConsumersInfo(String groupId){
        return kafkaService.getConsumersInfo(groupId);
    }

    public void send(String topic, String message){
        kafkaService.send(topic, message);
    }
}
