package plantsketch.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
        testButton.setOnAction(e -> showTestMode(stage, true));
        
        Button runButton = new Button("Run Mode");
        runButton.setPrefWidth(200);
        runButton.setPrefHeight(50);
        runButton.setStyle("-fx-font-size: 16px;");
        runButton.setOnAction(e -> showTestMode(stage, false));
        
        root.getChildren().addAll(title, subtitle, testButton, runButton);
        
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.show();
        centerStage(stage);
    }

    // this shows the first and second screens for both test mode and run mode
    private void showTestMode(Stage stage, boolean isTestGrid) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        
            Label sampleLabel;
            TextField sampleField = new TextField("1000"); // default value
            Label title;
            Label subtitle;
            Button randomButton;
            Button chooseFolderButton;
            Button preset1Button;
            Button preset2Button;
            Button preset3Button;
            Button preset4Button;

        if (isTestGrid)
        {
            title = new Label("Test Mode");
            subtitle = new Label("Select Test Configuration");
             preset1Button = new Button("Preset 1: Good Cond.");
             preset2Button = new Button("Preset 2: Harsh Cond.");
        }
        else 
        {
             title = new Label("Run Mode");
             subtitle = new Label("Select Environments");
             preset1Button = new Button("D1-256");
             preset2Button = new Button("D2-512");
        }

        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        subtitle.setStyle("-fx-font-size: 16px;");

        sampleLabel = new Label("Sample Count (1-10000):");
        sampleField.setPrefWidth(150);
        sampleField.setPrefHeight(40);
        
        preset1Button.setPrefWidth(150);
        preset1Button.setPrefHeight(40);
        preset1Button.setOnAction(e -> launchTestView(stage, "preset1", isTestGrid, sampleField));
        
        preset2Button.setPrefWidth(150);
        preset2Button.setPrefHeight(40);
        preset2Button.setOnAction(e -> launchTestView(stage, "preset2", isTestGrid, sampleField));
        
        Button backButton = new Button("Back");
        backButton.setPrefWidth(100);
        backButton.setOnAction(e -> showModeSelection(stage));

        if (!isTestGrid)
        {


            chooseFolderButton = new Button("Select a Folder");
            preset3Button = new Button("D3-1024");
            preset4Button = new Button("D4-1024");

            chooseFolderButton.setPrefWidth(150);
            chooseFolderButton.setPrefHeight(40);
            chooseFolderButton.setOnAction(e -> showWizard(stage, isTestGrid, sampleField));
            
            preset3Button.setPrefWidth(150);
            preset3Button.setPrefHeight(40);
            preset3Button.setOnAction(e -> launchTestView(stage, "preset3", isTestGrid, sampleField));
            
            preset4Button.setPrefWidth(150);
            preset4Button.setPrefHeight(40);
            preset4Button.setOnAction(e -> launchTestView(stage, "preset4", isTestGrid, sampleField));

            root.getChildren().addAll(title, subtitle, sampleLabel, sampleField, chooseFolderButton, preset1Button, preset2Button, preset3Button, preset4Button, backButton);
        }
        else
        {
            randomButton = new Button("Random");
            randomButton.setPrefWidth(150);
            randomButton.setPrefHeight(40);
            randomButton.setOnAction(e -> launchTestView(stage, "random", isTestGrid, sampleField));

            root.getChildren().addAll(title, subtitle, randomButton, preset1Button, preset2Button, backButton);
        }
        
        Scene scene = new Scene(root, 400, 350);
        stage.setScene(scene);
    }

    /** Launch the TestView with selected configuration */
    private void launchTestView(Stage stage, String mode, boolean isTestGrid, TextField sampleField) {

    
//          getting sample count
        int sampleCount = Integer.parseInt(sampleField.getText().trim());
        if (sampleCount <= 0) {
            sampleCount = 1;
        }
        if (sampleCount >= 10000)
        {
            sampleCount = 10000;
        }

        TestView testView = new TestView(() -> showModeSelection(stage), mode, isTestGrid, sampleCount);
        Scene scene = new Scene(testView, 1400, 900);
        stage.setScene(scene);
        stage.show();
        centerStage(stage);
        
        // Run simulation after scene is shown
        Platform.runLater(() -> testView.initializeWithMode(mode));
    }

    /** Show the wizard for custom folder selection */
    private void showWizard(Stage stage, boolean isTestGrid, TextField sampleField) {
        StartupWizard wizard = new StartupWizard(stage, (dataRoot, envFolder, sampleCount) -> {
            // Create TestView with custom folder configuration
            TestView testView = new TestView(() -> showModeSelection(stage), "customFolder", isTestGrid, sampleCount);
            Scene scene = new Scene(testView, 1400, 900);
            stage.setScene(scene);
            stage.show();
            centerStage(stage);

            // Initialize with the custom folder data
            Platform.runLater(() -> testView.initializeWithCustomFolder(dataRoot, envFolder));
        });
        
        // Display the wizard
        Scene wizardScene = new Scene(wizard, 600, 400);
        stage.setScene(wizardScene);
        stage.show();
        centerStage(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}