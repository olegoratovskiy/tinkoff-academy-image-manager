package com.example.imageapi.resourse;

import com.example.imageapi.auth.JwtAuthenticationFilter;
import com.example.imageapi.auth.JwtService;
import com.example.imageapi.config.security.SecurityConfiguration;
import com.example.imageapi.domain.Image;
import com.example.imageapi.domain.ImageMapper;
import com.example.imageapi.domain.ImageMapperImpl;
import com.example.imageapi.dto.GetImagesResponse;
import com.example.imageapi.dto.ImageResponse;
import com.example.imageapi.dto.UiSuccessContainer;
import com.example.imageapi.dto.UploadImageResponse;
import com.example.imageapi.service.ImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void downloadImage() {
        when(mockImageService.downloadImage(any())).thenReturn("file".getBytes());
        mockMvc.perform(get("/api/v1/image/id"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().bytes("file".getBytes()));
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