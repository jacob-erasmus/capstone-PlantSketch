
// this class is the gui for the testing grid (2x2)

package plantsketch.ui;

import plantsketch.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;

public class TestView extends BorderPane {
    
    private final Runnable onBack;
    private TestGrid testGrid;
    private SimulationResult currentResult;
    
    // UI Components
    private final TabPane tabs = new TabPane();
    private final ConsolePane console = new ConsolePane();
    private final VBox parameterPanel = new VBox(10);
    private final Label statusLabel = new Label();
    private final CheckBox regenerateSpeciesCheckBox = new CheckBox("Re-generate species at a point?");
    
    // Grid value editors
    private final Map<String, float[][]> currentGridValues = new HashMap<>();
    private final Map<String, GridEditor> gridEditors = new HashMap<>();
    
    public TestView(Runnable onBack) {
        this.onBack = onBack;
        this.testGrid = new TestGrid(console::log);
        
        setupUI();
        console.hookSystemStreams();
    }
    
    private void setupUI() {
        // Top toolbar
        setTop(buildToolbar());
        
        // Center - split between tabs and console
        var logHeader = buildLogHeader();
        var logBox = new VBox(logHeader, console.getNode());
        
        var split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);
        split.getItems().addAll(tabs, logBox);
        split.setDividerPositions(0.65);
        
        // Main content - tabs on left, parameter panel on right
        HBox mainContent = new HBox(10);
        mainContent.getChildren().addAll(split, buildParameterPanel());
        HBox.setHgrow(split, Priority.ALWAYS);
        
        setCenter(mainContent);
        
        // Status bar at bottom
        setBottom(buildStatusBar());
        
        setPadding(new Insets(8));
    }
    
    private ToolBar buildToolbar() {
        Button backBtn = new Button("Back to Mode Selection");
        backBtn.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });
        
        Label title = new Label("Test Mode - 2x2 Grid");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        return new ToolBar(backBtn, new Separator(), title);
    }
    
    private HBox buildLogHeader() {
        HBox header = new HBox();
        header.setSpacing(8);
        header.setPadding(new Insets(4, 0, 4, 0));
        Label label = new Label("Console Log");
        header.getChildren().add(label);
        return header;
    }
    
    private VBox buildParameterPanel() {
        parameterPanel.setPadding(new Insets(10));
        parameterPanel.setPrefWidth(500);
        parameterPanel.setStyle("-fx-border-color: #ccc; -fx-border-width: 1;");
        
        Label title = new Label("Parameter Controls");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(parameterPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(700);
        
        // Create grid editors for each parameter
        gridEditors.put("Temperature", new GridEditor("Temperature (°C)", 
            testGrid.getMinTemp(), testGrid.getMaxTemp()));
        gridEditors.put("Age", new GridEditor("Age (years)", 
            testGrid.getMinAge(), testGrid.getMaxAge()));
        gridEditors.put("Moisture", new GridEditor("Moisture (%)", 
            testGrid.getMinMoist(), testGrid.getMaxMoist()));
        gridEditors.put("Sunlight", new GridEditor("Sunlight (hours)", 
            testGrid.getMinSun(), testGrid.getMaxSun()));
        gridEditors.put("Elevation", new GridEditor("Elevation (m)", 
            testGrid.getMinElev(), testGrid.getMaxElev()));
        gridEditors.put("Slope", new GridEditor("Slope (degrees)", 
            testGrid.getMinSlope(), testGrid.getMaxSlope()));
        
        GridPane gridPane = new GridPane();
        gridPane.setHgap(15);  // space between columns
        gridPane.setVgap(10);  // space between rows
        gridPane.setPadding(new Insets(10));

       int col = 0, row = 0;
        for (GridEditor editor : gridEditors.values()) {
            gridPane.add(editor, col, row);

            col++;
            if (col > 1) { // wrap after 2 columns
                col = 0;
                row++;
            }
        }

        Button simulateBtn = new Button("Simulate with new parameters");
        simulateBtn.setPrefWidth(250);
        simulateBtn.setOnAction(e -> resimulate());

        VBox panelContent = new VBox(10);
        panelContent.getChildren().addAll
        (
            title,
            new Separator(),
            gridPane,
            new Separator(),
            regenerateSpeciesCheckBox,
            simulateBtn);
        

        scrollPane.setContent(panelContent);
        
        VBox container = new VBox(scrollPane);
        return container;
    }
    
    private HBox buildStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f0f0f0;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel.setFont(Font.font("Monospace", 11));
        statusBar.getChildren().add(statusLabel);
        
        return statusBar;
    }
    
    public void initializeWithMode(String mode) {
        console.clear();
        console.log("Initializing Test Mode: " + mode);
        
        switch (mode) {
            case "random":
                runRandomTest();
                break;
            case "preset1":
                runPreset1Test();
                break;
            case "preset2":
                runPreset2Test();
                break;
        }
    }
    
    private void runRandomTest() {
        executeSimulation(0);
    }
    
    private void runPreset1Test() {
        executeSimulation(1);
    }
    
    private void runPreset2Test() {
        executeSimulation(2);
    }

// here is execute simulation for the first time
    private void executeSimulation(int choice) {
        tabs.getTabs().clear();
        
        try {
            currentResult = testGrid.run(choice);
            
            // Update grid editors with current values
            updateGridEditors();
            
            // Create visualization tabs
            tabs.getTabs().add(makeTab("Pink Noise",
                new ScrollPane(new PinkNoiseView(currentResult.samples(), 
                    currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()))));
            
            tabs.getTabs().add(makeTab("Forest",
                new ScrollPane(new ForestView(currentResult.forest(), 
                    currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()))));
            
            tabs.getTabs().add(makeTab("Forest + Elevation",
                new ScrollPane(new ForestOnTerrainView(currentResult.forest(), 
                    currentResult.elevationGrid(), currentResult.gridSpacing()))));
            
            updateStatusDisplay();
            console.log("✓ Test simulation complete. Plants placed: " + 
                currentResult.forest().getAllPlants().size());
            
        } catch (Exception ex) {
            ex.printStackTrace();
            console.log("✗ Test simulation failed: " + ex.getMessage());
        }
    }
    
// and obvs here is the resimulation of the ting
    private void resimulate() {
        console.log("Re-simulating with new parameters...");
        
        // Update TestGrid with new values
        testGrid.setTemperatureGrid(gridEditors.get("Temperature").getValues());
        testGrid.setAgeGrid(gridEditors.get("Age").getValues());
        testGrid.setMoistureGrid(gridEditors.get("Moisture").getValues());
        testGrid.setSunlightGrid(gridEditors.get("Sunlight").getValues());
        testGrid.setElevationGrid(gridEditors.get("Elevation").getValues());
        testGrid.setSlopeGrid(gridEditors.get("Slope").getValues());
        
        if (regenerateSpeciesCheckBox.isSelected()) {
            // Full re-run with new species placement
            executeSimulation(0);
        } else {
            // Recalculate with same species positions
            testGrid.recalculateWithSameSpecies();
            currentResult = testGrid.makeSimResult();
            updateVisualization();
        }
    }
    
    private void updateVisualization() {
        tabs.getTabs().clear();
        
        tabs.getTabs().add(makeTab("Pink Noise",
            new ScrollPane(new PinkNoiseView(currentResult.samples(), 
                currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()))));
        
        tabs.getTabs().add(makeTab("Forest",
            new ScrollPane(new ForestView(currentResult.forest(), 
                currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()))));
        
        tabs.getTabs().add(makeTab("Forest + Elevation",
            new ScrollPane(new ForestOnTerrainView(currentResult.forest(), 
                currentResult.elevationGrid(), currentResult.gridSpacing()))));
        
        updateStatusDisplay();
        console.log("✓ Re-simulation complete. Plants: " + 
            currentResult.forest().getAllPlants().size());
    }
    
    private void updateGridEditors() {
        gridEditors.get("Temperature").setValues(testGrid.getTemperatureGrid());
        gridEditors.get("Age").setValues(testGrid.getAgeGrid());
        gridEditors.get("Moisture").setValues(testGrid.getMoistureGrid());
        gridEditors.get("Sunlight").setValues(testGrid.getSunlightGrid());
        gridEditors.get("Elevation").setValues(testGrid.getElevationGrid());
        gridEditors.get("Slope").setValues(testGrid.getSlopeGrid());
    }
    
    private void updateStatusDisplay() {
        StringBuilder status = new StringBuilder();
        status.append("Grid Status | ");
        status.append("Samples: ").append(testGrid.getSampleCount()).append(" | ");
        status.append("Pink Noise: ").append(testGrid.getNumPinkNoise()).append(" | ");
        status.append("Plants: ").append(testGrid.getNumPlants()).append("\n");
        
        status.append("Temp: ").append(formatGrid(testGrid.getTemperatureGrid())).append(" | ");
        status.append("Age: ").append(formatGrid(testGrid.getAgeGrid())).append(" | ");
        status.append("Moist: ").append(formatGrid(testGrid.getMoistureGrid())).append("\n");
        status.append("Sun: ").append(formatGrid(testGrid.getSunlightGrid())).append(" | ");
        status.append("Elev: ").append(formatGrid(testGrid.getElevationGrid())).append(" | ");
        status.append("Slope: ").append(formatGrid(testGrid.getSlopeGrid()));
        
        statusLabel.setText(status.toString());
    }
    
    private String formatGrid(float[][] grid) {
        if (grid == null || grid.length < 2) return "N/A";
        return String.format("[%.1f,%.1f;%.1f,%.1f]", 
            grid[0][0], grid[0][1], grid[1][0], grid[1][1]);
    }
    
    private Tab makeTab(String name, Node content) {
        Tab t = new Tab(name, content);
        t.setClosable(false);
        return t;
    }
    
    // Inner class for grid editing
    private class GridEditor extends VBox {
        private final String paramName;
        private final float min;
        private final float max;
        private final TextField[][] fields = new TextField[2][2];
        
        public GridEditor(String paramName, float min, float max) {
            this.paramName = paramName;
            this.min = min;
            this.max = max;
            
            setSpacing(5);
            
            Label label = new Label(paramName);
            label.setFont(Font.font("System", FontWeight.BOLD, 12));
            getChildren().add(label);
            
            GridPane grid = new GridPane();
            grid.setHgap(5);
            grid.setVgap(5);
            
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    fields[i][j] = new TextField();
                    fields[i][j].setPrefWidth(80);
                    fields[i][j].setPromptText(String.format("[%d,%d]", i+1, j+1));
                    grid.add(fields[i][j], j, i);
                }
            }
            
            Label rangeLabel = new Label(String.format("Range: %.1f - %.1f", min, max));
            rangeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
            
            getChildren().addAll(grid, rangeLabel);
        }
        
        public void setValues(float[][] values) {
            if (values != null && values.length >= 2) {
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        fields[i][j].setText(String.format("%.2f", values[i][j]));
                    }
                }
            }
        }
        
        public float[][] getValues() {
            float[][] values = new float[2][2];
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    try {
                        float val = Float.parseFloat(fields[i][j].getText());
                        values[i][j] = Math.max(min, Math.min(max, val));
                    } catch (NumberFormatException e) {
                        values[i][j] = (min + max) / 2;
                    }
                }
            }
            return values;
        }
    }
}