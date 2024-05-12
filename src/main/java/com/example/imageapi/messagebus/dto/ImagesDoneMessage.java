package com.example.imageapi.messagebus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Message for images.done topic.
 */
@Getter
@Setter
@NoArgsConstructor
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
