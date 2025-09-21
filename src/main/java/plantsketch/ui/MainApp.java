package plantsketch.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    // this makes sure that each window is always centered. yay! No more missing half of the screen
    private void centerStage(Stage stage) {
        // Get the screen dimensions
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        
        // Calculate the center position
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("PlantSketch");
        showModeSelection(primaryStage);
    }

    /** Show initial mode selection screen */
    private void showModeSelection(Stage stage) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        
        Label title = new Label("PlantSketch");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        
        Label subtitle = new Label("Select Mode");
        subtitle.setStyle("-fx-font-size: 18px;");
        
        Button testButton = new Button("Test Mode");
        testButton.setPrefWidth(200);
        testButton.setPrefHeight(50);
        testButton.setStyle("-fx-font-size: 16px;");
        testButton.setOnAction(e -> showTestMode(stage));
        
        Button runButton = new Button("Run Mode");
        runButton.setPrefWidth(200);
        runButton.setPrefHeight(50);
        runButton.setStyle("-fx-font-size: 16px;");
        runButton.setOnAction(e -> showWizard(stage));
        
        root.getChildren().addAll(title, subtitle, testButton, runButton);
        
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.show();
        centerStage(stage);
    }

    /** Show test mode selection */
    private void showTestMode(Stage stage) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        
        Label title = new Label("Test Mode");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label subtitle = new Label("Select Test Configuration");
        subtitle.setStyle("-fx-font-size: 16px;");
        
        Button randomButton = new Button("Random");
        randomButton.setPrefWidth(150);
        randomButton.setPrefHeight(40);
        randomButton.setOnAction(e -> launchTestView(stage, "random"));
        
        Button preset1Button = new Button("Preset 1: Good Cond.");
        preset1Button.setPrefWidth(150);
        preset1Button.setPrefHeight(40);
        preset1Button.setOnAction(e -> launchTestView(stage, "preset1"));
        
        Button preset2Button = new Button("Preset 2: Complex");
        preset2Button.setPrefWidth(150);
        preset2Button.setPrefHeight(40);
        preset2Button.setOnAction(e -> launchTestView(stage, "preset2"));
        
        Button backButton = new Button("Back");
        backButton.setPrefWidth(100);
        backButton.setOnAction(e -> showModeSelection(stage));
        
        root.getChildren().addAll(title, subtitle, randomButton, preset1Button, preset2Button, backButton);
        
        Scene scene = new Scene(root, 400, 350);
        stage.setScene(scene);
    }

    /** Launch the TestView with selected configuration */
    private void launchTestView(Stage stage, String mode) {
        TestView testView = new TestView(() -> showModeSelection(stage), mode);
        Scene scene = new Scene(testView, 1400, 900);
        stage.setScene(scene);
        stage.show();
        centerStage(stage);
        
        // Run simulation after scene is shown
        Platform.runLater(() -> testView.initializeWithMode(mode));
    }

    /** Show the original wizard for run mode */
    private void showWizard(Stage stage) {
        StartupWizard wizard = new StartupWizard(stage, (dataRoot, envFolder, sampleCount) -> {
            AppConfig.dataRoot = dataRoot;
            AppConfig.environment = envFolder;
            AppConfig.sampleCount = sampleCount;

            MainView mainView = new MainView(false, () -> showWizard(stage));
            Scene scene = new Scene(mainView, 1280, 800);
            stage.setScene(scene);
            stage.show();
            centerStage(stage);

            Platform.runLater(() ->
                mainView.runSimulation(AppConfig.dataRoot, AppConfig.environment, AppConfig.sampleCount)
            );
        });

        stage.setScene(new Scene(wizard, 820, 540));
        stage.show();
        centerStage(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}