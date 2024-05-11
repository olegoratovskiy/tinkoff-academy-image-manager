package com.example.imageapi.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer for images.wip topic.
 */
@Component
@RequiredArgsConstructor
public class ImagesWipSender {

    private final KafkaTemplate<String, ImagesWipMessage> imagesWipProducer;

    /**
     * Send message into images.wip topic.
     *
     * @param message message
     */
    public void sendMessage(final ImagesWipMessage message) {
        imagesWipProducer.send("images.wip", message);
    }

}
