package com.example.imageapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Image filter request not found exception.
 */
public class ImageFilterRequestNotFoundException extends ResponseStatusException {
    public ImageFilterRequestNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }
}
