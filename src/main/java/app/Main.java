package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static void showError(final Thread t, final Throwable e) {
        e.printStackTrace();
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Something went wrong...");
        alert.setContentText(e.getMessage());
        alert.show();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(Main::showError);
        final Parent root = FXMLLoader.load(getClass().getResource("../../resources/fxmls/mainWindow.fxml"));
        primaryStage.setTitle("Prelucrarea imaginilor - proiect „Ajustarea culorilor”");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
