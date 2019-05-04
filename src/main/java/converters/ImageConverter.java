package converters;

import domain.RGBImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageConverter {
    private ImageConverter() {
    }

    public static int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 255) {
            return 255;
        }
        return value;
    }

    public static RGBImage bufferedImageToRgbImage(final Image image) {
        final int height = (int) image.getHeight();
        final int width = (int) image.getWidth();
        final RGBImage rgbImage = new RGBImage(height, width);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                final int argb = image.getPixelReader().getArgb(i, j);
                final int red = (argb >> 16) & 0xff;
                final int green = (argb >> 8) & 0xff;
                final int blue = argb & 0xff;
                rgbImage.setPixel(j, i, red, green, blue);
            }
        }

        return rgbImage;
    }

    public static Image rgbImageToImage(final RGBImage rgbImage) {
        final int height = rgbImage.getHeight();
        final int width = rgbImage.getWidth();
        final WritableImage writableImage = new WritableImage(width, height);
        final PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final int r = rgbImage.getRedMatrix()[y][x];
                final int g = rgbImage.getGreenMatrix()[y][x];
                final int b = rgbImage.getBlueMatrix()[y][x];
                pixelWriter.setColor(x, y, Color.rgb(r, g, b));
            }
        }

        return writableImage;
    }
}
