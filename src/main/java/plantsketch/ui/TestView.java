
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
    private final CheckBox regeneratePinkNoise = new CheckBox("Re-generate pink noise?");
    // private final CheckBox regenerateSpecies = new CheckBox("Re-generate species placement?");
    
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
        
        // This creates the empty grids without the values inside
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

        // making two columns next to eachother of grid editors
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
        simulateBtn.setOnAction(e -> executeSimulation(0, true));

        VBox panelContent = new VBox(10);
        panelContent.getChildren().addAll
        (
            title,
            new Separator(),
            gridPane,
            new Separator(),
            regeneratePinkNoise,
            // regenerateSpecies,
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
    
    // This is just for the second screen with the options 
    public void initializeWithMode(String mode) {
        console.clear();
        console.log("Initializing Test Mode: " + mode);
        
        switch (mode) {
            case "random":
                executeSimulation(0, false);
                break;
            case "preset1":
                executeSimulation(1, false);
                break;
            case "preset2":
                executeSimulation(2, false);
                break;
        }
    }


// here is execute simulation for the first time
    private void executeSimulation(int choice, boolean isResimulation) {
        tabs.getTabs().clear();
        

        try {
            if(isResimulation)
            {
                console.log("Re-simulating with new parameters...");
            
                // Update TestGrid with new values
                testGrid.setTemperatureGrid(gridEditors.get("Temperature").getValues());
                testGrid.setAgeGrid(gridEditors.get("Age").getValues());
                testGrid.setMoistureGrid(gridEditors.get("Moisture").getValues());
                testGrid.setSunlightGrid(gridEditors.get("Sunlight").getValues());
                testGrid.setElevationGrid(gridEditors.get("Elevation").getValues());
                testGrid.setSlopeGrid(gridEditors.get("Slope").getValues());

                currentResult = testGrid.runChange(regeneratePinkNoise.isSelected() 
                // ,regenerateSpecies.isSelected()
                );
            }
            else
            {
            currentResult = testGrid.run(choice);
            
            }

            // Update grid editors with current values
            updateGridEditors();
            
            createTabs(currentResult);
            
            updateStatusDisplay();
            console.log("✓ Test simulation complete. Plants placed: " + currentResult.forest().getAllPlants().size());

            int numSpecies = 0;
            for (SpeciesMap sm : currentResult.forest().getOverallSpeciesMap()) 
            {
                console.log(" - " + sm.getPlants().size() + "  " + sm.getSpecies().getName() );
                if (sm.getPlants().size() > 0) numSpecies++;
            
                // this is for testing and confirming purposes
                for (Plant p : sm.getPlants()) 
                {
                    console.log("Plant " + p.getId() + " Species: " + p.getSpecies().getName() + " Vigour " + p.getVigour() + " Height: " + p.getHeight());
                    
                }
            }
            for (int row = 0; row < 2; row++) 
            {
                for (int col = 0; col < 2; col++) 
                {
                    console.log("Temperature gridEditor: " + gridEditors.get("Temperature").getValues()[row][col]);
                    console.log("Temperature testGrid: " + testGrid.getTemperatureGrid()[row][col]);
                }
            }
            
            console.log("Number of species: " + numSpecies);
            updateVisualization();

        } catch (Exception ex) 
        {
            ex.printStackTrace();
            console.log("✗ Test simulation failed: " + ex.getMessage());
        }
    }
    
    public void createTabs(SimulationResult currentResult)
    {
// Create visualization tabs

            tabs.getTabs().add(makeTab("Forest + Elevation", new ScrollPane(new ForestOnTerrainView(currentResult.forest(), currentResult.elevationGrid(), currentResult.gridSpacing()))));

            tabs.getTabs().add(makeTab("Pink Noise", new ScrollPane(new PinkNoiseView(currentResult.samples(), currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()))));
            
            tabs.getTabs().add(makeTab("Forest", new ScrollPane(new ForestView(currentResult.forest(), currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()))));
            

    }

    // this updates the three tabs on the top and makes the grids
    private void updateVisualization() {
        tabs.getTabs().clear();
        
        tabs.getTabs().add(makeTab("Forest + Elevation", new ScrollPane(new ForestOnTerrainView(currentResult.forest(), currentResult.elevationGrid(), currentResult.gridSpacing()))));

        tabs.getTabs().add(makeTab("Pink Noise", new ScrollPane(new PinkNoiseView(currentResult.samples(), currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()))));
        
        tabs.getTabs().add(makeTab("Forest", new ScrollPane(new ForestView(currentResult.forest(),  currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()))));
    
        updateStatusDisplay();
        console.log("Plants: " + currentResult.forest().getAllPlants().size());
    }
    

    private void updateGridEditors() {
        gridEditors.get("Temperature").setValues(testGrid.getTemperatureGrid());
        gridEditors.get("Age").setValues(testGrid.getAgeGrid());
        gridEditors.get("Moisture").setValues(testGrid.getMoistureGrid());
        gridEditors.get("Sunlight").setValues(testGrid.getSunlightGrid());
        gridEditors.get("Elevation").setValues(testGrid.getElevationGrid());
        gridEditors.get("Slope").setValues(testGrid.getSlopeGrid());
    }
    
    // This is the bottom status bar
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
    
    // formats the grid values for display in the bottom status bar
    private String formatGrid(float[][] grid) {
        if (grid == null || grid.length < 2) return "N/A";
        return String.format("[%.1f,%.1f;%.1f,%.1f]", grid[0][0], grid[0][1], grid[1][0], grid[1][1]);
    }
    
    private Tab makeTab(String name, Node content) {
        Tab t = new Tab(name, content);
        t.setClosable(false);
        return t;
    }
    
}