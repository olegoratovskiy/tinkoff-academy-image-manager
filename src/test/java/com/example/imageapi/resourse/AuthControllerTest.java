package com.example.imageapi.resourse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.imageapi.auth.AuthController;
import com.example.imageapi.auth.AuthenticationService;
import com.example.imageapi.auth.JwtAuthenticationFilter;
import com.example.imageapi.auth.dto.AuthResponse;
import com.example.imageapi.auth.dto.RegisterRequest;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
//@Import({AuthController.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @SneakyThrows
    void register() {
        var request = new ObjectMapper().writeValueAsString(
            new RegisterRequest("name", "email", "password")
        );
        var response = new ObjectMapper().writeValueAsString(
            new AuthResponse("token")
        );

        when(authenticationService.register(any())).thenReturn(new AuthResponse("token"));
        mockMvc.perform(post("/auth/sign-up").content(request).contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(response));
    }

}