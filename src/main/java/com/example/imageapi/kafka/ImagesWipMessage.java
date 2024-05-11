package com.example.imageapi.kafka;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Message for images.wip topic.
 */
@Getter
@Setter
@AllArgsConstructor
public class ImagesWipMessage {
    /**
     * ИД изображения с которым сейчас ведется работа по данному запросу.
     */
    private String imageId;
    /**
     * ИД пользовательского запроса.
     */
    private String requestId;
    /**
     * Фильтры, которые нужно применить.
     */
    private List<String> filters;
}
