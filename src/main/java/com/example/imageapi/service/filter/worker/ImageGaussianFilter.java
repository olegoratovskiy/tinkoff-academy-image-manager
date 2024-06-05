package com.example.imageapi.service.filter.worker;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

/**
 * Gaussian Filter.
 */
@Service
public class ImageGaussianFilter extends AbstractConcurrentFilter {
    private static final int kernelSize = 5;
    private static final double sigma = 5;

    /**
     * Apply filter.
     *
     * @param imageData image
     * @return result image
     */
    public byte[] apply(byte[] imageData) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            BufferedImage filteredImage = gaussianBlur(image);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(filteredImage, "jpeg", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BufferedImage gaussianBlur(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Calculate the kernel matrix
        double[][] kernel = calculateGaussianKernel();

        ExecutorService executorService = init();

        // Apply the Gaussian blur
        for (int y = 0; y < height; y++) {
            int finalY = y;
            executorService.submit(() -> {
                for (int x = 0; x < width; x++) {
                    double[] pixel = new double[3];
                    for (int ky = 0; ky < kernelSize; ky++) {
                        for (int kx = 0; kx < kernelSize; kx++) {
                            int imageX = (x - kernelSize / 2 + kx + width) % width;
                            int imageY = (finalY - kernelSize / 2 + ky + height) % height;
                            int rgb = image.getRGB(imageX, imageY);
                            pixel[0] += ((rgb >> 16) & 0xFF) * kernel[ky][kx];
                            pixel[1] += ((rgb >> 8) & 0xFF) * kernel[ky][kx];
                            pixel[2] += (rgb & 0xFF) * kernel[ky][kx];
                        }
                    }
                    filteredImage.setRGB(x, finalY,
                        ((int) pixel[0] << 16) | ((int) pixel[1] << 8) | (int) pixel[2]);
                }
            });
        }

        await(executorService);

        return filteredImage;
    }

    private static double[][] calculateGaussianKernel() {
        double[][] kernel = new double[kernelSize][kernelSize];
        double sum = 0;
        for (int y = 0; y < kernelSize; y++) {
            for (int x = 0; x < kernelSize; x++) {
                kernel[y][x] = gaussian(x - kernelSize / 2, y - kernelSize / 2);
                sum += kernel[y][x];
            }
        }
        // Normalize the kernel
        for (int y = 0; y < kernelSize; y++) {
            for (int x = 0; x < kernelSize; x++) {
                kernel[y][x] /= sum;
            }
        }
        return kernel;
    }

    private static double gaussian(double x, double y) {
        return Math.exp(-(x * x + y * y) / (2 * sigma * sigma)) / (2 * Math.PI * sigma * sigma);
    }
}
