package com.example.imageapi.resourse;

import com.example.imageapi.domain.Image;
import com.example.imageapi.domain.ImageMapper;
import com.example.imageapi.dto.ApplyImageFiltersResponse;
import com.example.imageapi.dto.GetImagesResponse;
import com.example.imageapi.dto.GetModifiedImageByRequestIdResponse;
import com.example.imageapi.dto.UiSuccessContainer;
import com.example.imageapi.dto.UploadImageResponse;
import com.example.imageapi.exception.NotSupportedFilterException;
import com.example.imageapi.service.ImageService;
import com.example.imageapi.service.filter.ImageFilter;
import com.example.imageapi.service.filter.ImageFiltersService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Images controller.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImageResource {

    private final ImageService service;
    private final ImageFiltersService filtersService;
    private final ImageMapper imageMapper;

    @PostMapping("/image")
    public UploadImageResponse upload(MultipartFile file) throws Exception {
        Image image = service.uploadImage(file);
        return new UploadImageResponse(image.getFileId());
    }

    @GetMapping(value = "/image/{imageId}")
    public @ResponseBody byte[] get(@PathVariable String imageId) throws Exception {
        return service.downloadImage(imageId);
    }

    @DeleteMapping("/image/{imageId}")
    public UiSuccessContainer delete(@PathVariable String imageId) throws Exception {
        service.deleteImage(imageId);
        return new UiSuccessContainer(true, null);
    }

    @GetMapping(value = "/images")
    public GetImagesResponse getImages() {
        var images = service.getAllImages().stream().map(imageMapper::toDto).toList();
        return new GetImagesResponse(images);
    }

    /**
     * Apply filters.
     *
     * @param imageId image
     * @param filters filters
     * @return result image
     */
    @RateLimiter(name = "ImageApi")
    @PostMapping(value = "/image/{imageId}/filters/apply")
    public ApplyImageFiltersResponse applyImageFilters(
        @PathVariable String imageId,
        @RequestParam List<String> filters
    ) {
        List<ImageFilter> imageFilters = new ArrayList<>();
        for (String filter : filters) {
            try {
                imageFilters.add(ImageFilter.valueOf(filter));
            } catch (IllegalArgumentException e) {
                throw new NotSupportedFilterException(
                    "Filter " + filter + " is not supported. Available filters are: "
                        + Arrays.stream(ImageFilter.values()).map(Enum::toString)
                        .collect(Collectors.joining(", ")));
            }
        }
        return new ApplyImageFiltersResponse(
            filtersService.applyImageFilters(imageId, imageFilters));
    }

    @GetMapping(value = "/image/{imageId}/filters/{requestId}")
    public GetModifiedImageByRequestIdResponse getApplyingImageFiltersStatus(
        @PathVariable String imageId,
        @PathVariable String requestId
    ) {
        return filtersService.getApplyingImageFiltersStatus(imageId, requestId);
    }
}
