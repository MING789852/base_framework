package com.xm.service.impl;

import com.xm.advice.exception.exception.CommonException;
import com.xm.core.kafka.KafkaConsumersInfo;
import com.xm.core.kafka.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaServiceImpl implements KafkaService {

    private final AdminClient adminClient;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public List<KafkaConsumersInfo> getConsumersInfo(String groupId){
        Map<TopicPartition, OffsetAndMetadata> consumerOffsets = null;
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> endOffsets = null;
        try {
            //指定要查询的消费者组
            ListConsumerGroupOffsetsResult result = adminClient.listConsumerGroupOffsets(groupId);
            //获取消费者组的当前偏移量
            consumerOffsets = result.partitionsToOffsetAndMetadata().get();
            //为每个分区准备查询末端偏移量（Log End Offset）
            Map<TopicPartition, OffsetSpec> topicPartitions = new HashMap<>();
            for (TopicPartition tp : consumerOffsets.keySet()) {
                topicPartitions.put(tp, OffsetSpec.latest());
            }
            // 6. 获取分区末端偏移量
            endOffsets = adminClient.listOffsets(topicPartitions).all().get();
            // 7. 计算每个分区的Lag（末端偏移量 - 当前消费偏移量）
            List<KafkaConsumersInfo> kafkaConsumersInfoList = new ArrayList<>();
            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : consumerOffsets.entrySet()) {
                TopicPartition tp = entry.getKey();
                long currentOffset = entry.getValue().offset();
                long endOffset = endOffsets.get(tp).offset();
                long partitionLag = endOffset - currentOffset;

                KafkaConsumersInfo kafkaConsumersInfo = new KafkaConsumersInfo();
                kafkaConsumersInfo.setTopic(tp.topic());
                kafkaConsumersInfo.setPartition(tp.partition());
                kafkaConsumersInfo.setCurrentOffset(currentOffset);
                kafkaConsumersInfo.setEndOffset(endOffset);
                kafkaConsumersInfo.setPartitionLag(partitionLag);
                kafkaConsumersInfo.setGroupId(groupId);
                kafkaConsumersInfoList.add(kafkaConsumersInfo);
            }
            return kafkaConsumersInfoList;
        }catch (Exception e){
            log.error("【kafka】获取消费者信息失败",e);
            throw new CommonException(e.getMessage());
        } finally {
            // 显式清理资源（如果需要）
            if (consumerOffsets != null) {
                consumerOffsets.clear();
            }
            if (endOffsets != null) {
                endOffsets.clear();
            }
        }
    }

    @Override
    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
