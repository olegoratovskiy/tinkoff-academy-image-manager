package com.example.imageapi.advice;

import com.example.imageapi.dto.UiSuccessContainer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@RequiredArgsConstructor
public class BaseExceptionHandler {
    @ExceptionHandler({ResponseStatusException.class})
    public ResponseEntity<UiSuccessContainer> statusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(new UiSuccessContainer(false, ex.getMessage()));
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<UiSuccessContainer> exception(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new UiSuccessContainer(false, ex.getMessage()));
    }
}
