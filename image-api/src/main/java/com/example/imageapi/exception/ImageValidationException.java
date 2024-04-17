package com.example.imageapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ImageValidationException extends ResponseStatusException {
    public ImageValidationException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }
}
