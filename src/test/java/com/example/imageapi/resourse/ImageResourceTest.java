package com.example.imageapi.resourse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.imageapi.auth.JwtAuthenticationFilter;
import com.example.imageapi.domain.Image;
import com.example.imageapi.domain.ImageMapperImpl;
import com.example.imageapi.dto.GetImagesResponse;
import com.example.imageapi.dto.ImageResponse;
import com.example.imageapi.dto.UiSuccessContainer;
import com.example.imageapi.dto.UploadImageResponse;
import com.example.imageapi.exception.ImageNotAvailableException;
import com.example.imageapi.exception.ImageNotFoundException;
import com.example.imageapi.exception.ImageValidationException;
import com.example.imageapi.service.ImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ImageResource.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ImageMapperImpl.class})
class ImageResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService mockImageService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @SneakyThrows
    void uploadImage() {
        when(mockImageService.uploadImage(any())).thenReturn(
                new Image("name", 1L, 1L, "file")
        );
        mockMvc.perform(post("/api/v1/image").content("file".getBytes()))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(
                        new ObjectMapper().writeValueAsString(new UploadImageResponse("file"))
                ));
    }

    @Test
    @SneakyThrows
    void uploadImageValidationException() {
        when(mockImageService.uploadImage(any())).thenThrow(new ImageValidationException("reason"));
        mockMvc.perform(post("/api/v1/image"))
            .andDo(print()).andExpect(status().isBadRequest())
            .andExpect(content().json(
                new ObjectMapper().writeValueAsString(new UiSuccessContainer(false,
                    "400 BAD_REQUEST \"reason\""))
            ));
    }

    @Test
    @SneakyThrows
    void downloadImage() {
        when(mockImageService.downloadImage(any())).thenReturn("file".getBytes());
        mockMvc.perform(get("/api/v1/image/id"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().bytes("file".getBytes()));
    }

    @Test
    @SneakyThrows
    void downloadImageNotFound() {
        when(mockImageService.downloadImage(any())).thenThrow(new ImageNotFoundException("reason"));
        mockMvc.perform(get("/api/v1/image/id"))
            .andDo(print()).andExpect(status().isNotFound())
            .andExpect(content().json(
                new ObjectMapper().writeValueAsString(new UiSuccessContainer(false,
                    "404 NOT_FOUND \"reason\""))
            ));
    }

    @Test
    @SneakyThrows
    void downloadImageNotAvailable() {
        when(mockImageService.downloadImage(any())).thenThrow(new ImageNotAvailableException("reason"));
        mockMvc.perform(get("/api/v1/image/id"))
            .andDo(print()).andExpect(status().isNotFound())
            .andExpect(content().json(
                new ObjectMapper().writeValueAsString(new UiSuccessContainer(false,
                    "404 NOT_FOUND \"reason\""))
            ));
    }

    @Test
    @SneakyThrows
    void downloadImageRuntimeException() {
        when(mockImageService.downloadImage(any())).thenThrow(new RuntimeException("reason"));
        mockMvc.perform(get("/api/v1/image/id"))
            .andDo(print()).andExpect(status().isInternalServerError())
            .andExpect(content().json(
                new ObjectMapper().writeValueAsString(new UiSuccessContainer(false,
                    "reason"))
            ));
    }

    @Test
    @SneakyThrows
    void deleteImage() {
        mockMvc.perform(delete("/api/v1/image/id"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(
                        new ObjectMapper().writeValueAsString(new UiSuccessContainer(true, null))
                ));
    }

    @Test
    @SneakyThrows
    void getImages() {
        when(mockImageService.getAllImages()).thenReturn(List.of(new Image(1, "name", 1L, 1L, "file")));
        mockMvc.perform(get("/api/v1/images"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(
                        new ObjectMapper().writeValueAsString(
                                new GetImagesResponse(List.of(
                                        new ImageResponse("name", 1L, "file")
                                ))
                        )
                ));
    }
}