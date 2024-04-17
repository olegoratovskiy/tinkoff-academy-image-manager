package com.example.imageapi.auth;

import com.example.imageapi.auth.dto.AuthResponse;
import com.example.imageapi.auth.dto.LoginRequest;
import com.example.imageapi.auth.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up")
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authenticationService.register(request);
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/sign-in")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authenticationService.login(request);
    }
}
