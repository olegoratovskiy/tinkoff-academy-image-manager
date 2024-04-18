package com.example.imageapi.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

/**
 * DTO for {@link com.example.imageapi.domain.Image}.
 */
@Getter
@Setter
@AllArgsConstructor
public class ImageResponse implements Serializable {
    String filename;
    Long size;
    String imageId;
}