package com.example.imageapi.messagebus;

import com.example.imageapi.messagebus.dto.ImagesDoneMessage;
import com.example.imageapi.messagebus.dto.ImagesWipMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Message sender.
 */
@Component
@RequiredArgsConstructor
public class Sender {

    private final KafkaTemplate<String, ImagesWipMessage> wipProducer;
    private final KafkaTemplate<String, ImagesDoneMessage> doneProducer;

    /**
     * Send message into images.wip topic.
     *
     * @param message message
     */
    public void sendWip(final ImagesWipMessage message) {
        wipProducer.send("images.wip", message);
    }

    /**
     * Send message into images.done topic.
     *
     * @param message message
     */
    public void sendDone(final ImagesDoneMessage message) {
        doneProducer.send("images.done", message);
    }
}
