package com.example.imageapi.messagebus;

import com.example.imageapi.domain.HandlerIdempotency;
import com.example.imageapi.domain.Image;
import com.example.imageapi.domain.ImageFilterRequest;
import com.example.imageapi.messagebus.dto.ImagesDoneMessage;
import com.example.imageapi.messagebus.dto.ImagesWipMessage;
import com.example.imageapi.repository.HandlerIdempotencyRepository;
import com.example.imageapi.repository.ImageFilterRequestRepository;
import com.example.imageapi.repository.ImageRepository;
import com.example.imageapi.service.MinioService;
import com.example.imageapi.service.filter.ImageFilter;
import com.example.imageapi.service.filter.ImageFilterStatus;
import com.example.imageapi.service.filter.worker.ImageAdaptiveThresholdingFilter;
import com.example.imageapi.service.filter.worker.ImageFilterWorker;
import com.example.imageapi.service.filter.worker.ImageGaussianFilter;
import com.example.imageapi.service.filter.worker.ImageMaximumFilter;
import com.example.imageapi.service.filter.worker.ImageSobelFilter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumers.
 */
@Component
@RequiredArgsConstructor
public class Handler {

    /**
     * Filter query repository.
     */
    private final ImageFilterRequestRepository imageFilterRequestRepository;

    private final HandlerIdempotencyRepository handlerIdempotencyRepository;

    private final MinioService imageStorage;

    private final Sender sender;

    private final ImageRepository imageRepository;

    private final Map<ImageFilter, ImageFilterWorker> filterWorkers = Map.of(
        ImageFilter.ADAPTIVE_THRESHOLDING, new ImageAdaptiveThresholdingFilter(),
        ImageFilter.GAUSSIAN, new ImageGaussianFilter(),
        ImageFilter.MAXIMUM, new ImageMaximumFilter(),
        ImageFilter.SOBEL, new ImageSobelFilter()
    );

    /**
     * Listener for images.done topic.
     *
     * @param record message
     * @param acknowledgment offset
     */
    @KafkaListener(
        topics = "images.done",
        groupId = "consumer-done",
        concurrency = "3"
    )
    public void handleDone(
        final ConsumerRecord<String, ImagesDoneMessage> record,
        final Acknowledgment acknowledgment
    ) {
        Optional<ImageFilterRequest> request = imageFilterRequestRepository
            .findByRequestId(record.value().getRequestId());
        if (request.isPresent()) {
            Optional<Image> image =
                imageRepository.findImageByFileId(request.get().getOriginalImageId());
            if (image.isPresent()) {
                imageRepository.save(new Image(
                    image.get().getName(),
                    image.get().getSize(), image.get().getUserId(),
                    record.value().getImageId())
                );
                imageFilterRequestRepository.save(request.get()
                    .setStatus(ImageFilterStatus.DONE)
                    .setModifiedImageId(record.value().getImageId())
                );
            }
        }
        acknowledgment.acknowledge();
    }

    /**
     * Listener for images.wip topic.
     *
     * @param record message
     * @param acknowledgment offset
     */
    @KafkaListener(
        topics = "images.wip",
        groupId = "consumer-wip-at",
        concurrency = "2"
    )
    public void handleWipAdaptiveThresholdingFilter(
        final ConsumerRecord<String, ImagesWipMessage> record,
        final Acknowledgment acknowledgment
    ) throws Exception {
        handleWip(record, acknowledgment, ImageFilter.ADAPTIVE_THRESHOLDING);
    }

    /**
     * Listener for images.wip topic.
     *
     * @param record message
     * @param acknowledgment offset
     */
    @KafkaListener(
        topics = "images.wip",
        groupId = "consumer-wip-gaussian",
        concurrency = "2"
    )
    public void handleWipGaussianFilter(
        final ConsumerRecord<String, ImagesWipMessage> record,
        final Acknowledgment acknowledgment
    ) throws Exception {
        handleWip(record, acknowledgment, ImageFilter.GAUSSIAN);
    }

    /**
     * Listener for images.wip topic.
     *
     * @param record message
     * @param acknowledgment offset
     */
    @KafkaListener(
        topics = "images.wip",
        groupId = "consumer-wip-maximum",
        concurrency = "2"
    )
    public void handleWipMaximumFilter(
        final ConsumerRecord<String, ImagesWipMessage> record,
        final Acknowledgment acknowledgment
    ) throws Exception {
        handleWip(record, acknowledgment, ImageFilter.MAXIMUM);
    }

    /**
     * Listener for images.wip topic.
     *
     * @param record message
     * @param acknowledgment offset
     */
    @KafkaListener(
        topics = "images.wip",
        groupId = "consumer-wip-sobel",
        concurrency = "2"
    )
    public void handleWipSobelFilter(
        final ConsumerRecord<String, ImagesWipMessage> record,
        final Acknowledgment acknowledgment
    ) throws Exception {
        handleWip(record, acknowledgment, ImageFilter.SOBEL);
    }

    private void handleWip(
        final ConsumerRecord<String, ImagesWipMessage> record,
        final Acknowledgment acknowledgment,
        final ImageFilter acceptedFilter
    ) throws Exception {
        String imageId = record.value().getImageId();
        String requestId = record.value().getRequestId();

        if (record.value().getFilters().isEmpty()
            || !Objects.equals(record.value().getFilters().get(0), acceptedFilter.toString())
            || handlerIdempotencyRepository.existsByRequestIdAndImageId(requestId, imageId)) {
            acknowledgment.acknowledge();
            return;
        }

        byte[] resultImage = filterWorkers.get(acceptedFilter)
            .apply(imageStorage.downloadImage(imageId));

        if (record.value().getFilters().size() == 1) {
            String resultImageId = imageStorage.uploadImage(resultImage);
            sender.sendDone(new ImagesDoneMessage(resultImageId, requestId));
        } else {
            String resultImageId = imageStorage.uploadTemporaryImage(resultImage);
            sender.sendWip(new ImagesWipMessage(
                resultImageId,
                requestId,
                record.value().getFilters().subList(1, record.value().getFilters().size()))
            );
        }
        handlerIdempotencyRepository.save(
            new HandlerIdempotency()
                .setRequestId(requestId)
                .setImageId(imageId)
        );
        acknowledgment.acknowledge();
    }

}
