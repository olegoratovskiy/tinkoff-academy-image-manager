package com.example.imageapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Image not available exception.
 */
public class ImageNotAvailableException extends ResponseStatusException {
    public ImageNotAvailableException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }
}
