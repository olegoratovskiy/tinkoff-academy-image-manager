package com.example.imageapi.domain;

import com.example.imageapi.service.filter.ImageFilterStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Image filter request model.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "image_filter_request")
public class ImageFilterRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String requestId;

    private ImageFilterStatus status;

    private String originalImageId;

    private String modifiedImageId;

    /**
     * ImageFilterRequest constructor.
     *
     * @param requestId request id.
     * @param status status.
     * @param originalImageId id of image.
     */
    public ImageFilterRequest(String requestId, ImageFilterStatus status, String originalImageId) {
        this.requestId = requestId;
        this.status = status;
        this.originalImageId = originalImageId;
    }
}
