package plantsketch.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("PlantSketch");
        showWizard(primaryStage);
    }

    /** Centralized navigator: show the startup wizard. */
    private void showWizard(Stage stage) {
        StartupWizard wizard = new StartupWizard(stage, (dataRoot, envFolder, sampleCount) -> {
            AppConfig.dataRoot = dataRoot;
            AppConfig.environment = envFolder;
            AppConfig.sampleCount = sampleCount;

            // Build the main view WITHOUT the toolbar, and give it a way to go "back" to the wizard
            MainView mainView = new MainView(false, () -> showWizard(stage));
            Scene scene = new Scene(mainView, 1280, 800);
            stage.setScene(scene);
            stage.show();

            // Run after the scene is visible so the log console can stream messages
            Platform.runLater(() ->
                mainView.runSimulation(AppConfig.dataRoot, AppConfig.environment, AppConfig.sampleCount)
            );
        });

        stage.setScene(new Scene(wizard, 820, 540));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
