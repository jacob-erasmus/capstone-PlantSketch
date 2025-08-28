package plantsketch.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        var root = new MainView(); // our JavaFX UI
        var scene = new Scene(root, 1200, 800);
        stage.setTitle("PlantSketch");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
