package com.example.imageapi.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Message for images.done topic.
 */
@Getter
@Setter
@AllArgsConstructor
public class ImagesDoneMessage {
    /**
     * ИД итогового изображения.
     */
    private String imageId;
    /**
     * ИД пользовательского запроса.
     */
    private String requestId;
}
