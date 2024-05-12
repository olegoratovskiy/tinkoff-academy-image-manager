package com.example.imageapi.service.filter.worker;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import javax.imageio.ImageIO;

/**
 * Adaptive Thresholding Filter.
 */
public class ImageAdaptiveThresholdingFilter extends AbstractConcurrentFilter {

    private static final int blockSize = 5;
    private static final int c = 5;

    /**
     * Apply filter.
     *
     * @param imageData image
     * @return result image
     */
    public byte[] apply(byte[] imageData) {
        try {
            BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(imageData));
            int width = inputImage.getWidth();
            int height = inputImage.getHeight();

            BufferedImage outputImage = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_RGB
            );

            ExecutorService executorService = init();

            for (int y = 0; y < height; y++) {
                int finalY = y;
                executorService.submit(() -> {
                    for (int x = 0; x < width; x++) {
                        int[] threshold = calculateAdaptiveThreshold(inputImage, x, finalY);
                        int rgb = (threshold[0] << 16) | (threshold[1] << 8) | threshold[2];
                        outputImage.setRGB(x, finalY, rgb);
                    }
                });
            }

            await(executorService);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(outputImage, "jpeg", outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private static int[] calculateAdaptiveThreshold(BufferedImage image, int x, int y) {
        int halfBlockSize = blockSize / 2;

        int sumRed = 0;
        int sumGreen = 0;
        int sumBlue = 0;
        for (int j = -halfBlockSize; j <= halfBlockSize; j++) {
            for (int i = -halfBlockSize; i <= halfBlockSize; i++) {
                int rgb = image.getRGB(
                    clamp(x + i, 0, image.getWidth() - 1),
                    clamp(y + j, 0, image.getHeight() - 1)
                );
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                sumRed += red;
                sumGreen += green;
                sumBlue += blue;
            }
        }
        int meanRed = sumRed / (blockSize * blockSize);
        int meanGreen = sumGreen / (blockSize * blockSize);
        int meanBlue = sumBlue / (blockSize * blockSize);

        // Calculate threshold
        int[] threshold = new int[3];
        int rgb = image.getRGB(x, y);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        threshold[0] = (red > (meanRed - c)) ? 255 : 0;
        threshold[1] = (green > (meanGreen - c)) ? 255 : 0;
        threshold[2] = (blue > (meanBlue - c)) ? 255 : 0;

        return threshold;
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
}
