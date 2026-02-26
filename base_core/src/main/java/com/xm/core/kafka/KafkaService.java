package com.xm.core.kafka;

import java.util.List;

public interface KafkaService {
    List<KafkaConsumersInfo> getConsumersInfo(String groupId);
    void send(String topic, String message);
}
