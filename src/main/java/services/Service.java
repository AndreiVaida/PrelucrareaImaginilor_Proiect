package services;

import converters.ImageConverter;
import domain.RGBImage;
import domain.RGBPixel;

public class Service {

    public void autoWhiteBalance(final RGBImage rgbImage) {
        // 1. detect a pixel that should be white
        final RGBPixel whiterPixel = detectWhiterPixel(rgbImage);
        whiterPixel.setRGB(250, 230, 252);

        // 2. correct white balance
        if (whiterPixel.getRed() == 255 && whiterPixel.getGreen() == 255 && whiterPixel.getBlue() == 255) {
            return;
        }
        final double redFactor = 255.0 / whiterPixel.getRed();
        final double greenFactor = 255.0 / whiterPixel.getGreen();
        final double blueFactor = 255.0 / whiterPixel.getBlue();
        correctWhiteBalance(rgbImage, redFactor, greenFactor, blueFactor);
    }

    private void correctWhiteBalance(final RGBImage rgbImage, final double redFactor, final double greenFactor, final double blueFactor) {
        final double[][] correctionMatrix = new double[3][3];
        correctionMatrix[0][0] = redFactor;
        correctionMatrix[1][1] = greenFactor;
        correctionMatrix[2][2] = blueFactor;

        final int height = rgbImage.getHeight();
        final int width = rgbImage.getWidth();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final int red = rgbImage.getRedMatrix()[i][j];
                final int green = rgbImage.getGreenMatrix()[i][j];
                final int blue = rgbImage.getBlueMatrix()[i][j];
                final int [][] pixels = new int[3][1];
                pixels[0][0] = red;
                pixels[1][0] = green;
                pixels[2][0] = blue;

                final int[][] newPixels = multiplyMatrices(correctionMatrix, pixels, 3, 3, 1);
                final int newRed = ImageConverter.clamp(newPixels[0][0]);
                final int newGreen = ImageConverter.clamp(newPixels[1][0]);
                final int newBlue = ImageConverter.clamp(newPixels[2][0]);
                rgbImage.setPixel(i, j, newRed, newGreen, newBlue);
            }
        }
    }

    private RGBPixel detectWhiterPixel(final RGBImage rgbImage) {
        final int height = rgbImage.getHeight();
        final int width = rgbImage.getWidth();
        final RGBPixel whiterPixel = new RGBPixel();
        int whiterAverage = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final int red = rgbImage.getRedMatrix()[i][j];
                final int green = rgbImage.getGreenMatrix()[i][j];
                final int blue = rgbImage.getBlueMatrix()[i][j];
                final int average = (red + green + blue) / 3;

                if (average > whiterAverage) {
                    whiterAverage = average;
                    whiterPixel.setRGB(red, green, blue);

                    if (whiterAverage == 255) {
                        return whiterPixel;
                    }
                }
            }
        }

        return whiterPixel;
    }

    public static int[][] multiplyMatrices(final double[][] matrixA, final int[][] matrixB, final int heightA, final int widthA, final int widthB) {
        int[][] matrixC = new int[heightA][widthB];
        for(int i = 0; i < heightA; i++) {
            for (int j = 0; j < widthB; j++) {
                for (int k = 0; k < widthA; k++) {
                    matrixC[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }
        return matrixC;
    }
}
