package com.example.imageapi.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for user login.
 */
@Setter
@Getter
@AllArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}
