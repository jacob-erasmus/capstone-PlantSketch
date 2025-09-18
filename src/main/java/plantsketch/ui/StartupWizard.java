package plantsketch.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StartupWizard extends BorderPane {

    public interface OnComplete {
        void handle(Path dataRoot, String environmentFolder, int sampleCount);
    }

    private final Stage stage;
    private final OnComplete onComplete;

    // Step 1
    private final Label pickedFolderLabel = new Label("No folder selected.");
    private Path chosenRoot;

    // Step 2
    private final ListView<String> envList = new ListView<>();

    // Step 3
    private final TextField samplesField = new TextField("2000");

    private int step = 1;

    public StartupWizard(Stage stage, OnComplete onComplete) {
        this.stage = stage;
        this.onComplete = onComplete;

        setPadding(new Insets(16));
        setTop(header("Welcome to PlantSketch!"));
        setCenter(step1());
        setBottom(navBar());
    }

    /* ---------- Header ---------- */
    private Node header(String title) {
        Label h = new Label(title);
        h.setStyle("-fx-font-size: 22px; -fx-font-weight: 700;");
        BorderPane.setAlignment(h, Pos.CENTER);
        BorderPane.setMargin(h, new Insets(0, 0, 12, 0));
        return h;
    }

    /* ---------- Nav Bar ---------- */
    private HBox navBar() {
        Button back = new Button("Back");
        Button next = new Button("Next");
        Button finish = new Button("Start");

        back.setOnAction(e -> goBack());
        next.setOnAction(e -> goNext());
        finish.setOnAction(e -> finish());

        back.setDisable(true);
        finish.setVisible(false);

        back.setUserData("back");
        next.setUserData("next");
        finish.setUserData("finish");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(8, back, spacer, next, finish);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(12, 0, 0, 0));
        return bar;
    }

    private void updateNavBarForStep() {
        HBox bar = (HBox) getBottom();
        Button back = (Button) bar.getChildren().stream().filter(n -> "back".equals(n.getUserData())).findFirst().orElse(null);
        Button next = (Button) bar.getChildren().stream().filter(n -> "next".equals(n.getUserData())).findFirst().orElse(null);
        Button finish = (Button) bar.getChildren().stream().filter(n -> "finish".equals(n.getUserData())).findFirst().orElse(null);

        if (back == null || next == null || finish == null) return;

        back.setDisable(step == 1);
        next.setVisible(step < 3);
        finish.setVisible(step == 3);
    }

    private void goBack() {
        if (step <= 1) return;
        step--;
        switch (step) {
            case 1 -> {
                setTop(header("Welcome to PlantSketch!"));
                setCenter(step1());
            }
            case 2 -> {
                setTop(header("Which environment would you like to render?"));
                setCenter(step2());
            }
        }
        updateNavBarForStep();
    }

    private void goNext() {
        if (step == 1) {
            if (chosenRoot == null || !Files.isDirectory(chosenRoot)) {
                alert("Please select the folder where the data for the program is stored.\n" +
                      "This folder should contain subfolders like D1-256, D2-512, etc.");
                return;
            }
            loadEnvironmentFolders(chosenRoot);
            if (envList.getItems().isEmpty()) {
                alert("No environment folders found in:\n" + chosenRoot + "\n\n" +
                      "Expected child folders such as D1-256, D2-512, etc.");
                return;
            }
            step = 2;
            setTop(header("Which environment would you like to render?"));
            setCenter(step2());

        } else if (step == 2) {
            if (envList.getSelectionModel().getSelectedItem() == null) {
                alert("Please select an environment folder (e.g., D1-256).");
                return;
            }
            step = 3;
            setTop(header("How many samples would you like to place?"));
            setCenter(step3());
        }
        updateNavBarForStep();
    }

    private void finish() {
        String env = envList.getSelectionModel().getSelectedItem();
        int n;
        try {
            n = Integer.parseInt(samplesField.getText().trim());
            if (n <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            alert("Please enter a positive integer for sample count.");
            return;
        }
        onComplete.handle(chosenRoot, env, n);
    }

// williams 2x2 test mode stuff
/*
    public void testMode(Stage stage)
    {
        Button test = new Button("Enter test mode:");

        test.setOnAction(e -> {

        })
    }

    /* ---------- Step 1: Pick data root ---------- */
    private Node step1() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label blurb = new Label("""
                Please select the folder where the data for the program is stored.
                This folder should contain environment subfolders such as D1-256, D2-512, etc.
                """);
        blurb.setWrapText(true);

        Button choose = new Button("Choose Folder…");
        choose.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Select data root (contains D1-256, D2-512, …)");
            if (chosenRoot != null && Files.isDirectory(chosenRoot)) {
                File init = chosenRoot.toFile();
                if (init.exists() && init.isDirectory()) {
                    dc.setInitialDirectory(init);
                }
            }
            File f = dc.showDialog(stage);
            if (f != null && f.isDirectory()) {
                chosenRoot = f.toPath();
                pickedFolderLabel.setText(chosenRoot.toString());
            }
        });

        pickedFolderLabel.setStyle("-fx-font-family: Consolas, monospace;");

        box.getChildren().addAll(blurb, choose, pickedFolderLabel);
        return padded(box);
    }

    /* ---------- Step 2: Pick environment subfolder ---------- */
    private Node step2() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label tip = new Label("Select one environment folder below:");
        tip.setWrapText(true);

        envList.setPrefHeight(260);

        box.getChildren().addAll(tip, envList);
        return padded(box);
    }

    private void loadEnvironmentFolders(Path root) {
        try {
            List<String> names;
            try (var stream = Files.list(root)) {
                names = stream
                        .filter(Files::isDirectory)
                        .map(p -> p.getFileName().toString())
                        .sorted(Comparator.naturalOrder())
                        .collect(Collectors.toList());
            }
            envList.getItems().setAll(names);
            if (!names.isEmpty()) {
                envList.getSelectionModel().select(0);
            }
        } catch (Exception ex) {
            alert("Failed to list folders in:\n" + root + "\n\n" + ex.getMessage());
        }
    }

    /* ---------- Step 3: Sample count ---------- */
    private Node step3() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label("Number of samples to place:");
        samplesField.setPromptText("e.g. 2000");
        samplesField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) finish();
        });

        box.getChildren().addAll(lbl, samplesField);
        return padded(box);
    }

    /* ---------- Helpers ---------- */
    private Node padded(Node n) {
        VBox wrap = new VBox(n);
        wrap.setPadding(new Insets(8, 4, 4, 4));
        return wrap;
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.initOwner(stage);
        a.setHeaderText(null);
        a.setTitle("PlantSketch");
        a.showAndWait();
    }


}
