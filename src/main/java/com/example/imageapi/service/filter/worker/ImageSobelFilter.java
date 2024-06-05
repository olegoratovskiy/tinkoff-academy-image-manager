package com.example.imageapi.service.filter.worker;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

/**
 * Sobel Filter.
 */
@Service
public class ImageSobelFilter extends AbstractConcurrentFilter {
    private static final int[][] SOBEL_X = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
    private static final int[][] SOBEL_Y = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

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
                BufferedImage.TYPE_BYTE_GRAY
            );

            ExecutorService executorService = init();

            for (int y = 1; y < height - 1; y++) {
                int finalY = y;
                executorService.submit(() -> {
                    for (int x = 1; x < width - 1; x++) {
                        int gx = calculateGradient(inputImage, x, finalY, SOBEL_X);
                        int gy = calculateGradient(inputImage, x, finalY, SOBEL_Y);
                        int gradient = (int) Math.sqrt(gx * gx + gy * gy);
                        gradient = Math.min(gradient, 255);
                        outputImage.setRGB(x, finalY, gradient << 16 | gradient << 8 | gradient);
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

    private static int calculateGradient(BufferedImage image, int x, int y, int[][] kernel) {
        int gx = 0;
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                int pixel = image.getRGB(x + i - 1, y + j - 1) & 0xFF;
                gx += pixel * kernel[j][i];
            }
        }
        return gx;
    }

}
