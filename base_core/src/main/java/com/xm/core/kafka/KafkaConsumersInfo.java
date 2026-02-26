package com.xm.core.kafka;

import lombok.Data;

@Data
public class KafkaConsumersInfo {
    private String topic;
    private String groupId;
    private int partition;
    private long currentOffset;
    private long partitionLag;
    private long endOffset;
}
