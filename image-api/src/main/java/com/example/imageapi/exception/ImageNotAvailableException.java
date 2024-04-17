package com.example.imageapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ImageNotAvailableException extends ResponseStatusException {
    public ImageNotAvailableException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }
}
