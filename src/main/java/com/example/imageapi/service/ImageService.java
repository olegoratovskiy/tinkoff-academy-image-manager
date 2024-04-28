package com.example.imageapi.service;

import com.example.imageapi.auth.UserService;
import com.example.imageapi.domain.Image;
import com.example.imageapi.exception.ImageNotAvailableException;
import com.example.imageapi.exception.ImageNotFoundException;
import com.example.imageapi.exception.ImageValidationException;
import com.example.imageapi.repository.ImageRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Image service.
 */
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final MinioService minioService;
    private final UserService userService;

    private static final long MAX_IMAGE_SIZE = 10000000;

    /**
     * Get all images uploaded by current user.
     *
     * @return list of user images.
     */
    public List<Image> getAllImages() {
        long userId = getCurrentUserId();

        return imageRepository.findAllByUserId(userId);
    }

    /**
     * Download image with given id.
     *
     * @param imageId image id.
     * @return image in bytes.
     * @throws Exception read-write exception.
     */
    public byte[] downloadImage(String imageId) throws Exception {
        validateImageAccess(imageId);

        return minioService.downloadImage(imageId);
    }

    /**
     * Upload image.
     *
     * @param file image.
     * @return image entity.
     * @throws Exception read-write or validation exception.
     */
    public Image uploadImage(MultipartFile file) throws Exception {
        if (file.getSize() > MAX_IMAGE_SIZE
            || !List.of(MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE)
            .contains(file.getContentType())
        ) {
            throw new ImageValidationException(
                "Provided file is too big or has not supported type.");
        }

        String fileId = minioService.uploadImage(file);

        String fileName = file.getOriginalFilename();
        Long fileSize = file.getSize();
        long userId = getCurrentUserId();

        return imageRepository.save(new Image(fileName, fileSize, userId, fileId));
    }

    /**
     * Delete image.
     *
     * @param imageId image id.
     * @throws Exception read-write or validation exception.
     */
    @Transactional
    public void deleteImage(String imageId) throws Exception {
        validateImageAccess(imageId);

        minioService.deleteImage(imageId);
        imageRepository.deleteByFileId(imageId);
    }

    public void validateImageAccess(String imageId) {
        Optional<Image> image = imageRepository.findImageByFileId(imageId);

        if (image.isEmpty()) {
            throw new ImageNotFoundException("Image with id " + imageId + " not found");
        }

        long userId = getCurrentUserId();
        if (!Objects.equals(image.get().getUserId(), userId)) {
            throw new ImageNotAvailableException("Access denied.");
        }
    }

    private long getCurrentUserId() {
        return userService.getCurrentUser().getId();
    }
}
