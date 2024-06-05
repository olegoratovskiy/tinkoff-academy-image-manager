package com.example.imageapi.service.filter.worker;

import com.example.imageapi.exception.RetryableException;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Image Tags Filter.
 */
@Service
public class ImageTagsFilter implements ImageFilterWorker {

    @Value("${imagga.authorization}")
    public String authorizationToken;

    private final WebClient client = WebClient.create();

    /**
     * Apply filter.
     *
     * @param imageData image
     * @return result image
     */
    @CircuitBreaker(name = "ImageTagsFilter")
    @Retry(name = "ImageTagsFilter")
    @RateLimiter(name = "ImageTagsFilter")
    public byte[] apply(byte[] imageData) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));

        List<String> tags = getTags(imageData);

        Font font = new Font("Arial", Font.BOLD, 30);

        Graphics g = image.getGraphics();
        g.setFont(font);
        g.setColor(Color.BLACK);
        int y = 50;
        for (String tag : tags) {
            g.drawString(tag, 20, y);
            y += 50;
        }
        g.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", outputStream);
        return outputStream.toByteArray();
    }

    private List<String> getTags(byte[] imageData) {

        Predicate<HttpStatusCode> statusPredicate =
            code -> code.is5xxServerError() || code.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS);

        String uploadId = Objects.requireNonNull(client.post()
            .uri("https://api.imagga.com/v2/uploads")
            .body(BodyInserters.fromMultipartData("image", imageData))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + authorizationToken)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(statusPredicate, (response) -> Mono.error(new RetryableException()))
            .bodyToMono(ResultUploadImageResponseDto.class)
            .block()).result.uploadId;

        List<TagResponseDto> tags = Objects.requireNonNull(client.get()
            .uri("https://api.imagga.com/v2/tags?image_upload_id=" + uploadId)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + authorizationToken)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(statusPredicate, (response) -> Mono.error(new RetryableException()))
            .bodyToMono(ResultGetTagsResponseDto.class)
            .block()).result.tags;

        return tags.stream()
            .sorted((a, b) -> Double.compare(b.confidence, a.confidence))
            .map(TagResponseDto::getTag)
            .map(TagValueResponseDto::getEn)
            .limit(3)
            .toList();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class ResultUploadImageResponseDto {
        UploadImageResponseDto result;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class ResultGetTagsResponseDto {
        GetTagsResponseDto result;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class UploadImageResponseDto {
        @JsonProperty("upload_id")
        String uploadId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class GetTagsResponseDto {
        List<TagResponseDto> tags;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class TagResponseDto {
        Double confidence;
        TagValueResponseDto tag;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class TagValueResponseDto {
        String en;
    }

}
