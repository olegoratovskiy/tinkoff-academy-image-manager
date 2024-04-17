package com.example.imageapi.dto;

import lombok.Value;

import java.util.List;

@Value
public class GetImagesResponse {
    List<ImageResponse> images;
}
