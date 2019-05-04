package controllers;

import converters.ImageConverter;
import domain.RGBImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public class MainWindowController {
    private final Service service;
    @FXML
    private AnchorPane mainAnchorPane;
    @FXML
    private ImageView toEditImageView;
    @FXML
    private ImageView editedImageView;
    @FXML
    private Label labelCurrentTransformationName;
    @FXML
    private HBox containerWhiteBalance;
    @FXML
    private HBox containerSaturation;
    @FXML
    private Slider sliderA;
    @FXML
    private Slider sliderSaturation;
    @FXML
    private Label labelASlider;
    @FXML
    private Label labelBSlider;
    @FXML
    private TextField textFieldWhiteBalance;
    @FXML
    private TextField textFieldSaturation;
    private Function<Void, Void> currentFilter;
    private Image originalImage;
    private Image toEditImage;
    private Image editedImage;
    private int a = 50;
    private int b = 200;
    private boolean changedByUser = true;

    public MainWindowController() {
        lab1Service = new Lab1Service();
        lab2Service = new Lab2Service();
        lab3Service = new Lab3Service();
        lab4Service = new Lab4Service();
        lab3Service.addObserver(this);
    }

    @FXML
    private void initialize() {
        loadDefaultImage();
        disableSliders();
        sliderA.setValue(a);
        sliderSaturation.setValue(b);
        textFieldWhiteBalance.setText(String.valueOf(a));
        textFieldSaturation.setText(String.valueOf(b));

        sliderA.valueProperty().addListener((observableValue, previousValue, newValue) -> {
            if (!changedByUser) {
                return;
            }
            a = newValue.intValue();
            textFieldWhiteBalance.setText(String.valueOf(a));
            currentFilter.apply(null);
        });
        sliderSaturation.valueProperty().addListener((observableValue, previousValue, newValue) -> {
            if (!changedByUser) {
                return;
            }
            b = newValue.intValue();
            textFieldSaturation.setText(String.valueOf(b));
            currentFilter.apply(null);
        });
    }

    public void changeWhiteBalanceTextFieldHandler(final KeyEvent keyEvent) {
        try {
            a = Integer.valueOf(textFieldWhiteBalance.getText());
            if (keyEvent.getCode() == KeyCode.ENTER) {
                sliderA.setValue(a);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    public void changeBTextFieldHandler(final KeyEvent keyEvent) {
        try {
            b = Integer.valueOf(textFieldSaturation.getText());
            if (keyEvent.getCode() == KeyCode.ENTER) {
                sliderSaturation.setValue(b);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private File openFileChooser() {
        final Stage stage = (Stage) mainAnchorPane.getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a JPEG image.");
        return fileChooser.showOpenDialog(stage);
    }

    private File saveFileChooser() {
        final Stage stage = (Stage) mainAnchorPane.getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JPEG image (*.jpg)", "*.jpg");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Save as");
        return fileChooser.showSaveDialog(stage);
    }

    private void loadImage(final File file) throws IOException {
        final BufferedImage bufferedImage = ImageIO.read(file);
        originalImage = SwingFXUtils.toFXImage(bufferedImage, null);
        toEditImage = originalImage;
        toEditImageView.setImage(toEditImage);
        editedImage = toEditImage;
        editedImageView.setImage(editedImage);
    }

    private void loadDefaultImage() {
        final File theatre = new File("./src/main/resources/images/Teatrul (low res).jpg");
        final File talisman = new File("./src/main/resources/images/Talisman (low res).jpg");
        final File biomedicalImage = new File("./src/main/resources/images/Radiografie (low res).jpg");
        final File apple = new File("./src/main/resources/images/Apple logo inverted.png");
        final File arrow = new File("./src/main/resources/images/left-arrow-inverted.jpg");
        final File bone = new File("./src/main/resources/images/bone.jpg");
        final File oldPhotograpther_saltAndPepper = new File("./src/main/resources/images/saltnpaperp3.jpg");
        final File f = new File("./src/main/resources/images/F.jpg");
        try {
            loadImage(oldPhotograpther_saltAndPepper);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadImageHandler(final ActionEvent actionEvent) throws IOException {
        final File file = openFileChooser();
        if (file != null) {
            loadImage(file);
        }
    }

    @FXML
    public void setEditedImageInToEditViewHandler(final MouseEvent mouseEvent) {
        toEditImage = editedImage;
        toEditImageView.setImage(toEditImage);
    }

    @FXML
    public void resetToOriginalHandler(final ActionEvent actionEvent) {
        toEditImage = originalImage;
        toEditImageView.setImage(toEditImage);
    }

    @FXML
    public void exportImageHandler(final ActionEvent actionEvent) throws IOException {
        final File file = saveFileChooser();
        final BufferedImage bi = SwingFXUtils.fromFXImage(editedImage, null);
        final BufferedImage bufferedImage = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        bufferedImage.getGraphics().drawImage(bi, 0, 0, null);
        ImageIO.write(bufferedImage, "jpg", file);
        System.out.println("Image saved: " + file.toString());
    }

    private void disableSliders() {
        containerWhiteBalance.setDisable(true);
        containerSaturation.setDisable(true);
        containerWhiteBalance.setOpacity(0);
        containerSaturation.setOpacity(0);
    }

    /* Curs 1 */
    @FXML
    public void convertToGreyscaleHandler(final ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("Convert to grayscale");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::convertToGreyscale;
        currentFilter.apply(null);
    }

    private Void convertToGreyscale(Void ignored) {
        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        final GreyscaleImage greyscaleImage = lab1Service.convertToGrayscale(rgbImage);
        editedImage = ImageConverter.grayscaleImageToImage(greyscaleImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    @FXML
    public void increaseLuminosityHandler(final ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("C1. Increase luminosity");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::increaseLuminosity;
        currentFilter.apply(null);
    }

    private Void increaseLuminosity(Void ignored) {
        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        lab1Service.increaseLuminosity(rgbImage);
        editedImage = ImageConverter.rgbImageToImage(rgbImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    /* Curs 2 */
    @FXML
    public void increaseContrastHandler(final ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("C2. Increase contrast");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::increaseContrast;
        currentFilter.apply(null);
    }

    private Void increaseContrast(Void ignored) {
        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        lab1Service.increaseContrast(rgbImage, a, b);
        editedImage = ImageConverter.rgbImageToImage(rgbImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    @FXML
    public void changeContrastHandler(final ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("Change contrast");
        changedByUser = false;
        containerWhiteBalance.setDisable(false);
        containerSaturation.setDisable(true);
        containerWhiteBalance.setOpacity(1);
        containerSaturation.setOpacity(0);
        sliderA.setMin(-255);
        sliderA.setMax(255);
        a = 0;
        sliderA.setValue(a);
        sliderA.setMajorTickUnit(25);
        textFieldWhiteBalance.setText(String.valueOf(a));
        labelASlider.setText("contrast");
        changedByUser = true;
        currentFilter = this::changeContrast;
        currentFilter.apply(null);
    }

    private Void changeContrast(Void ignored) {
        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        lab1Service.changeContrast(rgbImage, a);
        editedImage = ImageConverter.rgbImageToImage(rgbImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    /* Curs 3 */
    @FXML
    public void bitExtractionHandler(final ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("C3. Bit extraction");
        changedByUser = false;
        containerWhiteBalance.setDisable(false);
        containerSaturation.setDisable(true);
        containerWhiteBalance.setOpacity(1);
        containerSaturation.setOpacity(0);
        sliderA.setMin(0);
        sliderA.setMax(7);
        a = 0;
        sliderA.setValue(a);
        sliderA.setMajorTickUnit(1);
        sliderA.setMinorTickCount(0);
        textFieldWhiteBalance.setText(String.valueOf(a));
        labelASlider.setText("bit");
        changedByUser = true;
        currentFilter = this::bitExtraction;
        currentFilter.apply(null);
    }

    private Void bitExtraction(final Void ignored) {
        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        final GreyscaleImage greyscaleImage = lab1Service.bitExtraction(rgbImage, a);
        editedImage = ImageConverter.grayscaleImageToImage(greyscaleImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    /* Curs 4 */
    @FXML
    public void medianFilterHandler(final ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("C4. a.3) Filtrare mediană");
        changedByUser = false;
        containerWhiteBalance.setDisable(false);
        containerSaturation.setDisable(true);
        containerWhiteBalance.setOpacity(1);
        containerSaturation.setOpacity(0);
        sliderA.setMin(2);
        sliderA.setMax(5);
        a = 3;
        sliderA.setValue(a);
        sliderA.setMajorTickUnit(1);
        sliderA.setMinorTickCount(0);
        textFieldWhiteBalance.setText(String.valueOf(a));
        labelASlider.setText("Matrix size");
        changedByUser = true;
        currentFilter = this::medianFilter;
        currentFilter.apply(null);
    }

    private Void medianFilter(Void ignored) {
        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        lab2Service.medianFilter(rgbImage, a);
        editedImage = ImageConverter.rgbImageToImage(rgbImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    /* Curs 5 */
    @FXML
    public void invertContrastHandler(final ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("C5. d) Inversarea contrastului și scalarea statistică");
        changedByUser = false;
        disableSliders();
        a = 2;
        changedByUser = true;
        currentFilter = this::invertContrast;
        currentFilter.apply(null);
    }

    private Void invertContrast(Void ignored) {
        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        lab2Service.invertContrast(rgbImage, a);
        editedImage = ImageConverter.rgbImageToImage(rgbImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    @FXML
    public void pseudocolorImageHandler(final ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("C5. g) Pseudocolorarea imaginilor medicale");
        changedByUser = false;
        disableSliders();
        a = 2;
        changedByUser = true;
        currentFilter = this::pseudocolorImage;
        currentFilter.apply(null);
    }

    private Void pseudocolorImage(Void ignored) {
        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        final GreyscaleImage greyscaleImage = lab1Service.convertToGrayscale(rgbImage);
        final RGBImage coloredImage = lab2Service.pseudocolorImage(greyscaleImage);
        editedImage = ImageConverter.rgbImageToImage(coloredImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    @FXML
    public void identifyOutlineHandler(ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("C6. Contur");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::identifyOutlineAndAnimate;
        currentFilter.apply(null);
    }

    private Void identifyOutlineAndAnimate(Void aVoid) {
        editedImage = ImageConverter.duplicateImage(toEditImage);
        final BlackWhiteImage blackWhiteImage = ImageConverter.bufferedImageToBlackWhiteImage(toEditImage);
        final Outline outline = lab3Service.identifyOutline(blackWhiteImage);
        lab3Service.animateOutline_LineByLine(outline, 3);
        return null;
    }

    private void updateOnePixel(final int x, final int y, final Color color) {
        final WritableImage writableImage = (WritableImage) editedImage;
        final PixelWriter pixelWriter = writableImage.getPixelWriter();
        pixelWriter.setColor(x, y, color);
        editedImage = writableImage;
        editedImageView.setImage(editedImage);
    }

    @FXML
    public void convertToBlackWhiteHandler(ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("Convert to black and white");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::convertToBlackWhite;
        currentFilter.apply(null);
    }

    private Void convertToBlackWhite(Void aVoid) {
        final BlackWhiteImage blackWhiteImage = ImageConverter.bufferedImageToBlackWhiteImage(toEditImage);
        editedImage = ImageConverter.blackWhiteImageToImage(blackWhiteImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    @FXML
    public void identifySkeletonHandler(ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("C6. Schelet");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::identifySkeleton_Animate;
        currentFilter.apply(null);
    }

    private Void identifySkeleton(Void aVoid) {
        final BlackWhiteImage blackWhiteImage = ImageConverter.bufferedImageToBlackWhiteImage(toEditImage);
        final Skeleton skeleton = lab3Service.identifySkeleton(blackWhiteImage);
        // draw skeleton over image
        final int width = skeleton.getWidth();
        final int height = skeleton.getHeight();
        final WritableImage writableImage = new WritableImage(width, height);
        final PixelWriter pixelWriter = writableImage.getPixelWriter();
        final Color color = Color.rgb(0, 255, 0);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setColor(x, y, toEditImage.getPixelReader().getColor(x, y));
                if (skeleton.getMatrix()[y][x] == skeleton.getMaxHeight()) {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        editedImage = writableImage;
        editedImageView.setImage(editedImage);

        // fill
        fillSkeleton(skeleton);

        return null;
    }

    private Void identifySkeleton_Animate(Void aVoid) {
        editedImage = ImageConverter.duplicateImage(toEditImage);
        final BlackWhiteImage blackWhiteImage = ImageConverter.bufferedImageToBlackWhiteImage(toEditImage);
        new Thread(() -> lab3Service.identifySkeleton_Animate(blackWhiteImage, 20)).start();
        return null;
    }

    private void fillSkeleton(final Skeleton skeleton) {
        final int radius = skeleton.getMaxHeight();
        final Color circleColor = Color.rgb(0, 0, 255);
        for (int i = 0; i < skeleton.getHeight(); i++) {
            for (int j = 0; j < skeleton.getWidth(); j++) {
                if (skeleton.getMatrix()[i][j] == skeleton.getMaxHeight()) {
                    addCircleToImage(i, j, circleColor, radius);
                }
            }
        }
    }

    private void addCircleToImage(final int x, final int y, final Color colorToAdd, final int radius) {
        final WritableImage writableImage = (WritableImage) editedImage;
        final PixelWriter pixelWriter = writableImage.getPixelWriter();
        final int piR2 = (int) (Math.PI * (Math.pow(radius, 2)));

        for (int i = y - radius; i < y + radius; i++) {
            for (int j = x - radius; j < x + radius; j++) {
                if (i < 0 || j < 0 || i > writableImage.getHeight() || j > writableImage.getWidth()) {
                    continue;
                }

                if (pixelIsInCircle(j, i, x, y, radius)) {
                    pixelWriter.setColor(i, j, colorToAdd);
                }
            }
        }
        editedImage = writableImage;
        editedImageView.setImage(editedImage);
    }

    private boolean pixelIsInCircle(final int xPixel, final int yPixel, final int xCenter, final int yCenter, final int radius) {
        final int distanceFromCenter = (int) Math.sqrt(Math.pow(xPixel-xCenter, 2) + Math.pow(yPixel-yCenter, 2));
        return distanceFromCenter <= radius;
    }

    @FXML
    public void slimHandler(ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("C6. Contur");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::slimImage;
        currentFilter.apply(null);
    }

    private Void slimImage(Void aVoid) {
        editedImage = ImageConverter.duplicateImage(toEditImage);
        final BlackWhiteImage blackWhiteImage = ImageConverter.bufferedImageToBlackWhiteImage(toEditImage);
        lab3Service.slimImage_Animate(blackWhiteImage, 100);
        editedImage = ImageConverter.blackWhiteImageToImage(blackWhiteImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    @Override
    public void notifyOnEvent(final Event event) {
        if (event instanceof ChangePixelEvent) {
            final ChangePixelEvent changePixelEvent = (ChangePixelEvent) event;
            updateOnePixel(changePixelEvent.getX(), changePixelEvent.getY(), changePixelEvent.getColor());
        }
        if (event instanceof SkeletonFinishedEvent) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final SkeletonFinishedEvent skeletonFinishedEvent = (SkeletonFinishedEvent) event;
            fillSkeleton(skeletonFinishedEvent.getSkeleton());
        }
    }

    @FXML
    public void erosionBWHandler(ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("Eroziune (alb-negru)");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::erosionBW;
        currentFilter.apply(null);
    }

    private Void erosionBW(Void aVoid) {
        final BlackWhiteImage blackWhiteImage = ImageConverter.bufferedImageToBlackWhiteImage(toEditImage);
        final BlackWhiteImage blackWhiteImageEroded = lab4Service.erosionBW(blackWhiteImage);
        editedImage = ImageConverter.blackWhiteImageToImage(blackWhiteImageEroded);
        editedImageView.setImage(editedImage);
        return null;
    }

    @FXML
    public void dilationBWHandler(ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("Dilatare (alb-negru)");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::dilationBW;
        currentFilter.apply(null);
    }

    private Void dilationBW(Void aVoid) {
        final BlackWhiteImage blackWhiteImage = ImageConverter.bufferedImageToBlackWhiteImage(toEditImage);
        final BlackWhiteImage blackWhiteImageEroded = lab4Service.dilationBW(blackWhiteImage);
        editedImage = ImageConverter.blackWhiteImageToImage(blackWhiteImageEroded);
        editedImageView.setImage(editedImage);
        return null;
    }

    @FXML
    public void erosionGreyscaleHandler(ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("Eroziune (greyscale)");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::erosionGreyscale;
        currentFilter.apply(null);
    }

    private Void erosionGreyscale(Void aVoid) {
        final GreyscaleImage greyscaleImage = lab1Service.convertToGrayscale(ImageConverter.bufferedImageToRgbImage(toEditImage));
        final GreyscaleImage greyscaleImageEroded = lab4Service.erosionGreyscale(greyscaleImage);
        editedImage = ImageConverter.grayscaleImageToImage(greyscaleImageEroded);
        editedImageView.setImage(editedImage);
        return null;
    }

    @FXML
    public void dilationGreyscaleHandler(ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("Dilatare (greyscale)");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::dilationGreyscale;
        currentFilter.apply(null);
    }

    private Void dilationGreyscale(Void aVoid) {
        final GreyscaleImage greyscaleImage = lab1Service.convertToGrayscale(ImageConverter.bufferedImageToRgbImage(toEditImage));
        final GreyscaleImage greyscaleImageEroded = lab4Service.dilationGreyscale(greyscaleImage);
        editedImage = ImageConverter.grayscaleImageToImage(greyscaleImageEroded);
        editedImageView.setImage(editedImage);
        return null;
    }

    @FXML
    public void determineContourHandler(ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("C7. d) Determinarea conturului");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::determineContour;
        currentFilter.apply(null);
    }

    private Void determineContour(Void aVoid) {
        final BlackWhiteImage blackWhiteImage = ImageConverter.bufferedImageToBlackWhiteImage(toEditImage);
        final BlackWhiteImage blackWhiteImageEroded = lab4Service.determineContour(blackWhiteImage);
        editedImage = ImageConverter.blackWhiteImageToImage(blackWhiteImageEroded);
        editedImageView.setImage(editedImage);
        return null;
    }

    @FXML
    public void texturalSegmentationHandler(ActionEvent actionEvent) {
        labelCurrentTransformationName.setText("C8. f) Segmentare textuală");
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::texturalSegmentation;
        currentFilter.apply(null);
    }

    private Void texturalSegmentation(Void aVoid) {
        final GreyscaleImage greyscaleImage = lab1Service.convertToGrayscale(ImageConverter.bufferedImageToRgbImage(toEditImage));
        final GreyscaleImage greyscaleImageSegmented = lab4Service.texturalSegmentation(greyscaleImage);
        editedImage = ImageConverter.grayscaleImageToImage(greyscaleImageSegmented);
        editedImageView.setImage(editedImage);
        return null;
    }
}
