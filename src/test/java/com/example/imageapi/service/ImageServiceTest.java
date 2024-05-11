package com.example.imageapi.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.imageapi.auth.User;
import com.example.imageapi.auth.UserRepository;
import com.example.imageapi.auth.UserService;
import com.example.imageapi.domain.Image;
import com.example.imageapi.exception.ImageNotAvailableException;
import com.example.imageapi.exception.ImageNotFoundException;
import com.example.imageapi.exception.ImageValidationException;
import com.example.imageapi.repository.ImageFilterRequestRepository;
import com.example.imageapi.repository.ImageRepository;
import com.example.imageapi.service.filter.ImageFilterStatus;
import com.example.imageapi.service.filter.ImageFiltersService;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = {ImageServiceTest.Initializer.class})
class ImageServiceTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:latest")
                    .withDatabaseName("postgres")
                    .withUsername("user")
                    .withPassword("password");

    @Container
    public static MinIOContainer minIOContainer = new MinIOContainer("minio/minio:latest")
            .withUserName("user")
            .withPassword("password");

    @Container
    public static KafkaContainer kafkaContainer =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.2"))
            .withLogConsumer(new Slf4jLogConsumer(log));


    static {
        minIOContainer.setPortBindings(List.of("9000:9000"));
        kafkaContainer.setPortBindings(List.of("9093:9093", "29093:29093"));
    }

    @Component
    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                    "minio.url=" + minIOContainer.getS3URL(),
                    "minio.accessKey=" + minIOContainer.getUserName(),
                    "minio.secretKey=" + minIOContainer.getPassword(),
                    "minio.port=9000",
                    "minio.secure=false",
                    "minio.bucket=minio-storage",
                    "spring.kafka.listener.ack-mode=manual",
                    "spring.kafka.cloud.zookeeper.config.enabled=false",
                    "spring.kafka.cloud.zookeeper.connect-string=localhost:2181",
                    "spring.kafka.bootstrap_servers="+kafkaContainer.getBootstrapServers()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    private ImageService imageService;
    @Autowired
    private ImageFiltersService imageFiltersService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private ImageFilterRequestRepository imageFilterRequestRepository;
    @Autowired
    private MinioClient minioClient;

    private User initUser;

    @BeforeEach
    @SneakyThrows
    void init() {
        var user = new User();
        user.setUsername("user");
        user.setEmail("email");
        user.setPassword("password");
        user.setRole(User.Role.ROLE_USER);

        initUser = userService.create(user);
        minioClient.makeBucket(MakeBucketArgs.builder().bucket("minio-storage").build());
    }

    @AfterEach
    @SneakyThrows
    void clear() {
        userRepository.deleteAllInBatch();
        for (var bucket : minioClient.listBuckets()) {
            var objects = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucket.name()).build());
            for (var object : objects) {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucket.name())
                        .object(object.get().objectName())
                        .build()
                );
            }
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucket.name()).build());
        }
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "user")
    void getAllImages() {
        imageService.uploadImage(createTestImage("name1"));
        imageService.uploadImage(createTestImage("name2"));

        var images = imageService.getAllImages();
        assertEquals(2, images.size());
        assertEquals("name1", images.get(0).getName());
        assertEquals("name2", images.get(1).getName());
        assertEquals(userService.getCurrentUser(), initUser);
        assertEquals(userService.getCurrentUser().hashCode(), initUser.hashCode());
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "user")
    void downloadImage() {
        var testFile = createTestImage("name1");
        var image = imageService.uploadImage(testFile);

        assertArrayEquals(
                testFile.getBytes(),
                imageService.downloadImage(image.getFileId())
        );
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "user")
    void downloadImageNotAvailable() {
        imageRepository.save(new Image("name", 1L, 10L, "test"));

        assertThrows(
                ImageNotAvailableException.class,
                () -> imageService.downloadImage("test")
        );
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "user")
    void uploadImage() {
        var testFile = createTestImage("name1");
        var image = imageService.uploadImage(testFile);

        assertTrue(imageRepository.existsByFileId(image.getFileId()));
        assertEquals(testFile.getSize(), image.getSize());
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "user")
    void uploadImageValidationException() {
        var testFile = createTestImage("name1", MediaType.TEXT_PLAIN_VALUE);
        var testBigFile = createBigFile("name2");

        assertThrows(ImageValidationException.class, () -> imageService.uploadImage(testFile));
        assertThrows(ImageValidationException.class, () -> imageService.uploadImage(testBigFile));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "user")
    void deleteImage() {
        var testFile = createTestImage("name1");
        var image = imageService.uploadImage(testFile);

        imageService.deleteImage(image.getFileId());

        assertFalse(imageRepository.existsByFileId(image.getFileId()));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "user")
    void deleteImageNotFound() {
        assertThrows(ImageNotFoundException.class, () -> imageService.deleteImage("test"));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "user")
    void applyFilters() {
        var testFile = createTestImage("name1");
        var image = imageService.uploadImage(testFile);

        var requestId = imageFiltersService.applyImageFilters(image.getFileId(), List.of());
        var response = imageFiltersService.getApplyingImageFiltersStatus(image.getFileId(), requestId);

        assertTrue(imageFilterRequestRepository.existsByRequestId(requestId));
        assertEquals(ImageFilterStatus.WIP, response.getStatus());
        assertEquals(image.getFileId(), response.getImageId());
    }

    private MultipartFile createTestImage(String fileContent, String name, String mediaType) {
        return new MockMultipartFile(
                name,
                name,
                mediaType,
                fileContent.getBytes()
        );
    }

    private MultipartFile createTestImage(String name, String mediaType) {
        String fileContent = "some content";
        return createTestImage(fileContent, name, mediaType);
    }

    private MultipartFile createTestImage(String name) {
        return createTestImage(name, MediaType.IMAGE_PNG_VALUE);
    }

    private MultipartFile createBigFile(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        while (stringBuilder.length() < 2e7) {
            stringBuilder.append('a');
        }
        return createTestImage(stringBuilder.toString(), name, MediaType.IMAGE_PNG_VALUE);
    }
}