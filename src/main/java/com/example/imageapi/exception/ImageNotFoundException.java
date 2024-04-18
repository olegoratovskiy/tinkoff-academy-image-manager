package com.example.imageapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Image not found exception.
 */
public class ImageNotFoundException extends ResponseStatusException {
    public ImageNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }
}
