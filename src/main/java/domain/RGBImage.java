package domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RGBImage {
    private int height;
    private int width;
    private int[][] redMatrix;
    private int[][] greenMatrix;
    private int[][] blueMatrix;

    /**
     * Creates an empty RGB image with the given sizes.
     */
    public RGBImage(final int height, final int width) {
        this.height = height;
        this.width = width;
        redMatrix = new int[height][width];
        greenMatrix = new int[height][width];
        blueMatrix = new int[height][width];
    }

    public RGBImage(final RGBImage rgbImage) {
        height = rgbImage.height;
        width = rgbImage.width;
        redMatrix = new int[height][width];
        greenMatrix = new int[height][width];
        blueMatrix = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                redMatrix[i][j] = rgbImage.redMatrix[i][j];
                greenMatrix[i][j] = rgbImage.greenMatrix[i][j];
                blueMatrix[i][j] = rgbImage.blueMatrix[i][j];
            }
        }
    }

    public void setPixel(final int line, final int column, final int redComponent, final int greenComponent, final int blueComponent) {
        redMatrix[line][column] = redComponent;
        greenMatrix[line][column] = greenComponent;
        blueMatrix[line][column] = blueComponent;
    }
}
