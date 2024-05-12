package com.example.imageapi.service.filter.worker;

/**
 * Filter.
 */
public interface ImageFilterWorker {
    byte[] apply(byte[] imageData);
}
