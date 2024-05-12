package com.example.imageapi.service;

import com.example.imageapi.service.filter.worker.ImageAdaptiveThresholdingFilter;
import com.example.imageapi.service.filter.worker.ImageGaussianFilter;
import com.example.imageapi.service.filter.worker.ImageMaximumFilter;
import com.example.imageapi.service.filter.worker.ImageSobelFilter;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class ImageFilterTest {

    @Test
    @SneakyThrows
    public void testATFilter() {
        new ImageAdaptiveThresholdingFilter().apply(createImage());
    }

    @Test
    @SneakyThrows
    public void testGaussianFilter() {
        new ImageGaussianFilter().apply(createImage());
    }

    @Test
    @SneakyThrows
    public void testMaximumFilter() {
        new ImageMaximumFilter().apply(createImage());
    }

    @Test
    @SneakyThrows
    public void testSobelFilter() {
        new ImageSobelFilter().apply(createImage());
    }

    private byte[] createImage() throws IOException {
        var image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics();
        g.setColor(Color.blue);
        g.fillRect(0, 0, 100, 100);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", outputStream);
        return outputStream.toByteArray();
    }
}
