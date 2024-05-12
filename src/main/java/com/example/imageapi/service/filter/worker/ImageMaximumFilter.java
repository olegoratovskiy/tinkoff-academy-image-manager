package com.example.imageapi.service.filter.worker;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import javax.imageio.ImageIO;

/**
 * Maximum Filter.
 */
public class ImageMaximumFilter extends AbstractConcurrentFilter {
    private static final int filterSize = 5;

    /**
     * Apply filter.
     *
     * @param imageData image.
     * @return result image.
     */
    public byte[] apply(byte[] imageData) {
        try {
            BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(imageData));
            int width = inputImage.getWidth();
            int height = inputImage.getHeight();

            BufferedImage outputImage =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            ExecutorService executorService = init();

            for (int y = 0; y < height; y++) {
                int finalY = y;
                executorService.submit(() -> {
                    for (int x = 0; x < width; x++) {
                        int[] maxValues = getMaxValues(inputImage, x, finalY);
                        int rgb = (maxValues[0] << 16) | (maxValues[1] << 8) | maxValues[2];
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

    private static int[] getMaxValues(BufferedImage image, int x, int y) {
        int[] maxValues = new int[3]; // For RGB channels
        int halfFilterSize = filterSize / 2;

        for (int j = -halfFilterSize; j <= halfFilterSize; j++) {
            for (int i = -halfFilterSize; i <= halfFilterSize; i++) {
                int rgb = image.getRGB(
                    clamp(x + i, 0, image.getWidth() - 1),
                    clamp(y + j, 0, image.getHeight() - 1)
                );
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                maxValues[0] = Math.max(maxValues[0], red);
                maxValues[1] = Math.max(maxValues[1], green);
                maxValues[2] = Math.max(maxValues[2], blue);
            }
        }

        return maxValues;
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
}
