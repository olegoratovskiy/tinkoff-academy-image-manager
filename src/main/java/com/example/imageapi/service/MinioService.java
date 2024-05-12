package com.example.imageapi.service;

import com.example.imageapi.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketLifecycleArgs;
import io.minio.messages.Expiration;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import io.minio.messages.ResponseDate;
import io.minio.messages.RuleFilter;
import io.minio.messages.Status;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

/**
 * Minio storage service.
 */
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient client;
    private final MinioProperties properties;

    @PostConstruct
    private void init() throws Exception {
        if (!client.bucketExists(
            BucketExistsArgs.builder().bucket(properties.getBucket()).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
        }
        client.setBucketLifecycle(SetBucketLifecycleArgs
            .builder()
            .bucket(properties.getBucket())
            .config(new LifecycleConfiguration(
                List.of(new LifecycleRule(
                    Status.ENABLED,
                    null,
                    new Expiration((ResponseDate) null, properties.getTtlDays(), null),
                    new RuleFilter("tmp/"),
                    null, null, null, null))
            ))
            .build()
        );
    }

    /**
     * Upload image into storage.
     *
     * @param file image.
     * @return image id in storage.
     * @throws Exception read-write exception.
     */
    public String uploadImage(byte[] file) throws Exception {
        String fileId = UUID.randomUUID().toString();

        InputStream inputStream = new ByteArrayInputStream(file);
        client.putObject(
            PutObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(fileId)
                .stream(inputStream, file.length, properties.getImageSize())
                .build()
        );

        return fileId;
    }

    /**
     * Upload image into storage with ttl.
     *
     * @param file image.
     * @return image id in storage.
     * @throws Exception read-write exception.
     */
    public String uploadTemporaryImage(byte[] file) throws Exception {
        String fileId = "tmp/" + UUID.randomUUID();

        InputStream inputStream = new ByteArrayInputStream(file);
        client.putObject(
            PutObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(fileId)
                .stream(inputStream, file.length, properties.getImageSize())
                .build()
        );

        return fileId;
    }

    /**
     * Download image from storage.
     *
     * @param fileId image id.
     * @return image in bytes.
     * @throws Exception read-write exception.
     */
    public byte[] downloadImage(String fileId) throws Exception {
        return IOUtils.toByteArray(client.getObject(
            GetObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(fileId)
                .build()));
    }

    /**
     * Delete image from storage.
     *
     * @param fileId image id.
     * @throws Exception read-write exception.
     */
    public void deleteImage(String fileId) throws Exception {
        client.removeObject(
            RemoveObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(fileId)
                .build());
    }
}
