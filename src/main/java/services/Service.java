package services;

import domain.RGBImage;
import domain.RGBPixel;
import javafx.scene.paint.Color;

import static converters.ImageConverter.clamp;

public class Service {

    private static int[][] multiplyMatrices(final double[][] matrixA, final int[][] matrixB, final int heightA, final int widthA, final int widthB) {
        int[][] matrixC = new int[heightA][widthB];
        for (int i = 0; i < heightA; i++) {
            for (int j = 0; j < widthB; j++) {
                for (int k = 0; k < widthA; k++) {
                    matrixC[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }
        return matrixC;
    }

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

    public void autoWhiteBalanceBySelectedPixel(final RGBImage rgbImage, final Color selectedPixelColor) {
        final int red = (int) (selectedPixelColor.getRed() * 255);
        final int green = (int) (selectedPixelColor.getGreen() * 255);
        final int blue = (int) (selectedPixelColor.getBlue() * 255);

        if (red == 255 && green == 255 && blue == 255) {
            return;
        }
        final double redFactor = 255.0 / red;
        final double greenFactor = 255.0 / green;
        final double blueFactor = 255.0 / blue;
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
                final int[][] pixels = new int[3][1];
                pixels[0][0] = red;
                pixels[1][0] = green;
                pixels[2][0] = blue;

                final int[][] newPixels = multiplyMatrices(correctionMatrix, pixels, 3, 3, 1);
                final int newRed = clamp(newPixels[0][0]);
                final int newGreen = clamp(newPixels[1][0]);
                final int newBlue = clamp(newPixels[2][0]);
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

    /**
     * For each pixel:
     * r += adjustmentValue
     * g = g
     * b -= adjustmentValue
     */
    public void changeWhiteBalance_temperature(final RGBImage rgbImage, final int adjustmentValue) {
        final int height = rgbImage.getHeight();
        final int width = rgbImage.getWidth();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int red = rgbImage.getRedMatrix()[i][j];
                int green = rgbImage.getGreenMatrix()[i][j];
                int blue = rgbImage.getBlueMatrix()[i][j];

                red = clamp(red + adjustmentValue);
                blue = clamp(blue - adjustmentValue);
                rgbImage.setPixel(i, j, red, green, blue);
            }
        }
    }

    /**
     * For each pixel:
     * r = r
     * g = g - adjustmentValue
     * b = b
     */
    public void changeWhiteBalance_tint(final RGBImage rgbImage, final int adjustmentValue) {
        final int height = rgbImage.getHeight();
        final int width = rgbImage.getWidth();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int red = rgbImage.getRedMatrix()[i][j];
                int green = rgbImage.getGreenMatrix()[i][j];
                int blue = rgbImage.getBlueMatrix()[i][j];

                green = clamp(green - adjustmentValue);
                rgbImage.setPixel(i, j, red, green, blue);
            }
        }
    }

    public void changeSaturation(final RGBImage rgbImage, final int adjustmentValue) {
        final double redPerception = 0.299;
        final double greenPerception = 0.587;
        final double bluePerception = 0.114;

        final int height = rgbImage.getHeight();
        final int width = rgbImage.getWidth();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double redD = rgbImage.getRedMatrix()[i][j] / 255.0;
                double greenD = rgbImage.getGreenMatrix()[i][j] / 255.0;
                double blueD = rgbImage.getBlueMatrix()[i][j] / 255.0;

                final double brightness = Math.sqrt(redPerception * Math.pow(redD, 2)
                        + greenPerception * Math.pow(greenD, 2)
                        + bluePerception * Math.pow(blueD, 2));
                final double adjustmentValueD = adjustmentValue / 100.0 + 1;
                redD = brightness + (redD - brightness) * adjustmentValueD;
                greenD = brightness + (greenD - brightness) * adjustmentValueD;
                blueD = brightness + (blueD - brightness) * adjustmentValueD;

                final int red = clamp((int) (redD * 255));
                final int green = clamp((int) (greenD * 255));
                final int blue = clamp((int) (blueD * 255));
                rgbImage.setPixel(i, j, red, green, blue);
            }
        }
    }

    public void changeHue(final RGBImage rgbImage, final int adjustmentValue) {
        final int height = rgbImage.getHeight();
        final int width = rgbImage.getWidth();
        final double u = Math.cos(adjustmentValue * Math.PI / 180.0);
        final double w = Math.sin(adjustmentValue * Math.PI / 180.0);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final double redD = rgbImage.getRedMatrix()[i][j] / 255.0;
                final double greenD = rgbImage.getGreenMatrix()[i][j] / 255.0;
                final double blueD = rgbImage.getBlueMatrix()[i][j] / 255.0;
                final double newRedD = (.299 + .701 * u + .168 * w) * redD
                        + (.587 - .587 * u + .330 * w) * greenD
                        + (.114 - .114 * u - .497 * w) * blueD;
                final double newGreenD = (.299 - .299 * u - .328 * w) * redD
                        + (.587 + .413 * u + .035 * w) * greenD
                        + (.114 - .114 * u + .292 * w) * blueD;
                final double newBlueD = (.299 - .3 * u + 1.25 * w) * redD
                        + (.587 - .588 * u - 1.05 * w) * greenD
                        + (.114 + .886 * u - .203 * w) * blueD;

                final int red = clamp((int) (newRedD * 255));
                final int green = clamp((int) (newGreenD * 255));
                final int blue = clamp((int) (newBlueD * 255));
                rgbImage.setPixel(i, j, red, green, blue);
            }
        }

    }
}
