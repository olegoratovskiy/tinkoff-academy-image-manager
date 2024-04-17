package com.example.imageapi.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.example.imageapi.domain.Image}
 */
@Value
public class ImageResponse implements Serializable {
    String filename;
    Long size;
    String imageId;
}