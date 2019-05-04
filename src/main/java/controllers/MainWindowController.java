package controllers;

import converters.ImageConverter;
import domain.RGBImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
    private HBox containerWhiteBalance;
    @FXML
    private HBox containerSaturation;
    @FXML
    private Slider sliderWhiteBalance;
    @FXML
    private Slider sliderSaturation;
    @FXML
    private TextField textFieldWhiteBalance;
    @FXML
    private TextField textFieldSaturation;
    private Function<Void, Void> currentFilter;
    private Image originalImage;
    private Image toEditImage;
    private Image editedImage;
    private int whiteBalance = 50;
    private int saturation = 5000;
    private boolean changedByUser = true;

    public MainWindowController() {
        service = new Service();
    }

    @FXML
    private void initialize() {
        loadDefaultImage();
        disableSliders();
        sliderWhiteBalance.setValue(whiteBalance);
        sliderSaturation.setValue(saturation);
        textFieldWhiteBalance.setText(String.valueOf(whiteBalance));
        textFieldSaturation.setText(String.valueOf(saturation));

        sliderWhiteBalance.valueProperty().addListener((observableValue, previousValue, newValue) -> {
            if (!changedByUser) {
                return;
            }
            whiteBalance = newValue.intValue();
            textFieldWhiteBalance.setText(String.valueOf(whiteBalance));
            currentFilter.apply(null);
        });
        sliderSaturation.valueProperty().addListener((observableValue, previousValue, newValue) -> {
            if (!changedByUser) {
                return;
            }
            saturation = newValue.intValue();
            textFieldSaturation.setText(String.valueOf(saturation));
            currentFilter.apply(null);
        });
    }

    public void changeWhiteBalanceTextFieldHandler(final KeyEvent keyEvent) {
        try {
            whiteBalance = Integer.valueOf(textFieldWhiteBalance.getText());
            if (keyEvent.getCode() == KeyCode.ENTER) {
                sliderWhiteBalance.setValue(whiteBalance);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    public void changeBTextFieldHandler(final KeyEvent keyEvent) {
        try {
            saturation = Integer.valueOf(textFieldSaturation.getText());
            if (keyEvent.getCode() == KeyCode.ENTER) {
                sliderSaturation.setValue(saturation);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private File openFileChooser() {
        final Stage stage = (Stage) mainAnchorPane.getScene().getWindow();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select whiteBalance JPEG image.");
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

    private void disableSliders() {
        containerWhiteBalance.setDisable(true);
        containerSaturation.setDisable(true);
        containerWhiteBalance.setOpacity(0);
        containerSaturation.setOpacity(0);
    }

    /* Curs 1 */
    @FXML
    public void autoWhiteBalanceHandler(ActionEvent actionEvent) {
        changedByUser = false;
        disableSliders();
        changedByUser = true;
        currentFilter = this::autoWhiteBalance;
        currentFilter.apply(null);
    }

    private Void autoWhiteBalance(Void ignored) {
        final RGBImage rgbImage = ImageConverter.bufferedImageToRgbImage(toEditImage);
        service.autoWhiteBalance(rgbImage);
        editedImage = ImageConverter.rgbImageToImage(rgbImage);
        editedImageView.setImage(editedImage);
        return null;
    }
}
