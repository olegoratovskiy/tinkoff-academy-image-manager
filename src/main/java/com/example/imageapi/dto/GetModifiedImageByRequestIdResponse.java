package com.example.imageapi.dto;

import com.example.imageapi.service.filter.ImageFilterStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *  DTO for applying image filters status response.
 */
@Getter
@Setter
@AllArgsConstructor
public class GetModifiedImageByRequestIdResponse {
    String imageId;
    ImageFilterStatus status;
}
