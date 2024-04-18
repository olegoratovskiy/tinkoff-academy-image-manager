package com.example.imageapi.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for user registration.
 */
@Setter
@Getter
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
}
