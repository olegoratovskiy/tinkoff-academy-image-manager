package com.example.imageapi.advice;

import com.example.imageapi.dto.UiSuccessContainer;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception handler.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class BaseExceptionHandler {
    private final Counter apiFailureCounter = Metrics.counter("api-failure");

    @ExceptionHandler({ResponseStatusException.class})
    public ResponseEntity<UiSuccessContainer> statusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
            .body(new UiSuccessContainer(false, ex.getMessage()));
    }

    @ExceptionHandler({UsernameNotFoundException.class})
    public ResponseEntity<UiSuccessContainer> userNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new UiSuccessContainer(false, ex.getMessage()));
    }

    /**
     * Handle RateLimiter exception.
     *
     * @param ex exception
     * @return response
     */
    @ExceptionHandler({RequestNotPermitted.class})
    public ResponseEntity<UiSuccessContainer> requestNotPermitted(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new UiSuccessContainer(false,
                "You've exceeded the maximum number of request per one minute.")
            );
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<UiSuccessContainer> exception(RuntimeException ex) {
        apiFailureCounter.increment();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new UiSuccessContainer(false, ex.getMessage()));
    }
}
