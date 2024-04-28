package com.example.imageapi.service.filter;

import com.example.imageapi.domain.ImageFilterRequest;
import com.example.imageapi.dto.GetModifiedImageByRequestIdResponse;
import com.example.imageapi.exception.ImageFilterRequestNotFoundException;
import com.example.imageapi.repository.ImageFilterRequestRepository;
import com.example.imageapi.service.ImageService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Image filter service.
 */
@Service
@RequiredArgsConstructor
public class ImageFiltersService {

    private final ImageFilterRequestRepository imageFilterRequestRepository;
    private final ImageService imageService;

    public String applyImageFilters(String imageId, List<ImageFilter> imageFilters) {
        imageService.validateImageAccess(imageId);

        ImageFilterRequest imageFilterRequest = imageFilterRequestRepository
            .save(new ImageFilterRequest(
                UUID.randomUUID().toString(),
                ImageFilterStatus.WIP,
                imageId
            ));

        return imageFilterRequest.getRequestId();
    }

    public GetModifiedImageByRequestIdResponse getApplyingImageFiltersStatus(
        String imageId,
        String requestId
    ) {
        imageService.validateImageAccess(imageId);
        ImageFilterRequest imageFilterRequest =
            imageFilterRequestRepository.findByRequestId(requestId).orElseThrow(
                () -> new ImageFilterRequestNotFoundException(
                    "Image filter request with id " + requestId + " not found"));

        String responseImageId = imageFilterRequest.getModifiedImageId();
        if (responseImageId == null) {
            responseImageId = imageFilterRequest.getOriginalImageId();
        }
        return new GetModifiedImageByRequestIdResponse(responseImageId, imageFilterRequest.getStatus());
    }
}
