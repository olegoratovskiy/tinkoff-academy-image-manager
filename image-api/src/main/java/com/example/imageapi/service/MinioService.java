package com.example.imageapi.service;

import com.example.imageapi.config.MinioProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Minio storage service.
 */
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient client;
    private final MinioProperties properties;

    /**
     * Upload image into storage.
     *
     * @param file image.
     * @return image id in storage.
     * @throws Exception read-write exception.
     */
    public String uploadImage(MultipartFile file) throws Exception {
        String fileId = UUID.randomUUID().toString();

        InputStream inputStream = new ByteArrayInputStream(file.getBytes());
        client.putObject(
            PutObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(fileId)
                .stream(inputStream, file.getSize(), properties.getImageSize())
                .contentType(file.getContentType())
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
