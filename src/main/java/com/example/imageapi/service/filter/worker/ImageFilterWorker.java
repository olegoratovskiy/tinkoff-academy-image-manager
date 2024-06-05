package com.example.imageapi.service.filter.worker;

import java.io.IOException;

/**
 * Filter.
 */
public interface ImageFilterWorker {
    byte[] apply(byte[] imageData) throws IOException;
}
