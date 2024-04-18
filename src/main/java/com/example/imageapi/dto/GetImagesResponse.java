package com.example.imageapi.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

/**
 *  DTO for get images response.
 */
@Getter
@Setter
@AllArgsConstructor
public class GetImagesResponse {
    List<ImageResponse> images;
}
