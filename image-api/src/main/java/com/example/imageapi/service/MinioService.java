package com.example.imageapi.service;

import com.example.imageapi.config.MinioProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient client;
    private final MinioProperties properties;

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

    public byte[] downloadImage(String fileId) throws Exception {
        return IOUtils.toByteArray(client.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(fileId)
                        .build()));
    }

    public void deleteImage(String fileId) throws Exception {
        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(fileId)
                        .build());
    }
}
