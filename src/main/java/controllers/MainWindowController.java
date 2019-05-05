package controllers;

import converters.ImageConverter;
import domain.RGBImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.Service;

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
    public Pane paneSelectedPixel;
    @FXML
    public Label labelSelectedPixel;
    @FXML
    private Slider sliderWB_temperature;
    @FXML
    private Slider sliderWB_tint;
    @FXML
    private Slider sliderSaturation;
    @FXML
    private TextField textFieldWB_temperature;
    @FXML
    private TextField textFieldWB_tint;
    @FXML
    private TextField textFieldSaturation;
    private Function<Void, Void> currentFilter;
    private Image originalImage;
    private Image toEditImage;
    private Image editedImage;
    private Color selectedPixelColor;
    private int whiteBalance_temperatureValue;
    private int whiteBalance_tintValue;
    private int saturationValue;
    private boolean changedByUser = true;

    public MainWindowController() {
        service = new Service();
    }

    @FXML
    private void initialize() {
        loadDefaultImage();
        whiteBalance_temperatureValue = 0;
        whiteBalance_tintValue = 0;
        saturationValue = 0;
        sliderWB_temperature.setValue(whiteBalance_temperatureValue);
        sliderSaturation.setValue(saturationValue);
        textFieldWB_temperature.setText(String.valueOf(whiteBalance_temperatureValue));
        textFieldSaturation.setText(String.valueOf(saturationValue));

        sliderWB_temperature.valueProperty().addListener((observableValue, previousValue, newValue) -> {
            if (!changedByUser) {
                return;
            }
            whiteBalance_temperatureValue = newValue.intValue();
            textFieldWB_temperature.setText(String.valueOf(whiteBalance_temperatureValue));
            currentFilter = this::changeWB_temperature;
            currentFilter.apply(null);
        });
        sliderWB_tint.valueProperty().addListener((observableValue, previousValue, newValue) -> {
            if (!changedByUser) {
                return;
            }
            whiteBalance_tintValue = newValue.intValue();
            textFieldWB_tint.setText(String.valueOf(whiteBalance_tintValue));
            currentFilter = this::changeWB_tint;
            currentFilter.apply(null);
        });
        sliderSaturation.valueProperty().addListener((observableValue, previousValue, newValue) -> {
            if (!changedByUser) {
                return;
            }
            saturationValue = newValue.intValue();
            textFieldSaturation.setText(String.valueOf(saturationValue));
            currentFilter = this::changeSaturation;
            currentFilter.apply(null);
        });
    }

    private File openFileChooser() {
        final Stage stage = (Stage) mainAnchorPane.getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select whiteBalance_temperatureValue JPEG image.");
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
            loadImage(theatre);
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
        if (file == null) {
            return;
        }
        final BufferedImage bi = SwingFXUtils.fromFXImage(editedImage, null);
        final BufferedImage bufferedImage = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        bufferedImage.getGraphics().drawImage(bi, 0, 0, null);
        ImageIO.write(bufferedImage, "jpg", file);
        System.out.println("Image saved: " + file.toString());
    }

    @FXML
    public void pixelSelectHandler(MouseEvent mouseEvent) {
        final double divisionFactor;
        if (toEditImage.getWidth() > toEditImageView.getFitWidth()) {
            divisionFactor = toEditImage.getWidth() / toEditImageView.getFitWidth();
        } else {
            divisionFactor = toEditImage.getHeight() / toEditImageView.getFitHeight();
        }
        final int imageX = (int) (mouseEvent.getX() * divisionFactor);
        final int imageY = (int) (mouseEvent.getY() * divisionFactor);
        final Color color = toEditImage.getPixelReader().getColor(imageX, imageY);
        final int red = (int) (color.getRed() * 255);
        final int green = (int) (color.getGreen() * 255);
        final int blue = (int) (color.getBlue() * 255);
        final String rgbPixelColorString = "rgb(" + red + ", " + green + ", " + blue + ")";
        labelSelectedPixel.setText(rgbPixelColorString);
        paneSelectedPixel.setStyle("-fx-background-color: " + rgbPixelColorString);
        selectedPixelColor = Color.rgb(red, green, blue);
    }

    @FXML
    public void changeWB_temperatureTextFieldHandler(final KeyEvent keyEvent) {
        try {
            whiteBalance_temperatureValue = Integer.valueOf(textFieldWB_temperature.getText());
            if (keyEvent.getCode() == KeyCode.ENTER) {
                sliderWB_temperature.setValue(whiteBalance_temperatureValue);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    @FXML
    public void changeWB_tintTextFieldHandler(final KeyEvent keyEvent) {
        try {
            whiteBalance_tintValue= Integer.valueOf(textFieldWB_tint.getText());
            if (keyEvent.getCode() == KeyCode.ENTER) {
                sliderWB_tint.setValue(whiteBalance_tintValue);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    @FXML
    public void changeSaturationTextFieldHandler(final KeyEvent keyEvent) {
        try {
            saturationValue = Integer.valueOf(textFieldSaturation.getText());
            if (keyEvent.getCode() == KeyCode.ENTER) {
                sliderSaturation.setValue(saturationValue);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    // White Balance
    private Void changeWB_temperature(Void aVoid) {
        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        service.changeWhiteBalance_temperature(rgbImage, whiteBalance_temperatureValue);
        editedImage = ImageConverter.rgbImageToImage(rgbImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    private Void changeWB_tint(Void aVoid) {
        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        service.changeWhiteBalance_tint(rgbImage, whiteBalance_tintValue);
        editedImage = ImageConverter.rgbImageToImage(rgbImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    @FXML
    public void autoWhiteBalanceBySelectedPixelHandler(ActionEvent actionEvent) {
        currentFilter = this::autoWhiteBalanceBySelectedPixel;
        currentFilter.apply(null);
    }

    private Void autoWhiteBalanceBySelectedPixel(Void ignored) {
        if (selectedPixelColor == null) {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No selected pixel");
            alert.setContentText("Please select white color first");
            alert.show();
            return null;
        }

        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        service.autoWhiteBalanceBySelectedPixel(rgbImage, selectedPixelColor);
        editedImage = ImageConverter.rgbImageToImage(rgbImage);
        editedImageView.setImage(editedImage);
        return null;
    }

    // Saturation
    private Void changeSaturation(Void aVoid) {
        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        service.changeSaturation(rgbImage, saturationValue);
        editedImage = ImageConverter.rgbImageToImage(rgbImage);
        editedImageView.setImage(editedImage);
        return null;
    }
}
