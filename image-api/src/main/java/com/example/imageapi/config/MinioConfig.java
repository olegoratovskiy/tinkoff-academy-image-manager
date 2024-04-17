package com.example.imageapi.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *  Minio storage configuration.
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    /**
     * Minio client bean creation.
     *
     * @param properties application properties.
     * @return MinioClient bean.
     */
    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .endpoint(properties.getUrl(), properties.getPort(), properties.isSecure())
                .build();
    }
}
