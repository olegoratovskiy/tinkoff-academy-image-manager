package com.example.imageapi.kafka;

import com.example.imageapi.repository.ImageFilterRequestRepository;
import com.example.imageapi.service.filter.ImageFilterStatus;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumer for images.done topic.
 */
@Component
@RequiredArgsConstructor
public class ImagesDoneConsumer {

    /**
     * Filter query repository.
     */
    private final ImageFilterRequestRepository imageFilterRequestRepository;

    /**
     * Listener.
     *
     * @param record         received message.
     * @param acknowledgment object for pushing offset.
     */
    @KafkaListener(
        topics = "images.done",
        groupId = "consumer-done",
        concurrency = "2",
        properties = {
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG + "=false",
            ConsumerConfig.ISOLATION_LEVEL_CONFIG + "=read_committed",
            ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG
                + "=org.apache.kafka.clients.consumer.RoundRobinAssignor"
        }
    )
    public void consume(
        final ConsumerRecord<String, ImagesDoneMessage> record,
        final Acknowledgment acknowledgment
    ) {
        imageFilterRequestRepository
            .findByRequestId(record.value().getRequestId())
            .ifPresent(request -> imageFilterRequestRepository
                .save(request
                    .setStatus(ImageFilterStatus.DONE)
                    .setModifiedImageId(record.value().getImageId())
                )
            );

        acknowledgment.acknowledge();
    }

}
