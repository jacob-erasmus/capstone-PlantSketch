
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

public class TestAndRunView extends BorderPane {
    

//*********** NOTES AND TO DO ****************\\

// Error in remove species. null pointer exception because accessing species map in forest that dont exist (i think)
// in test grid: only update forest if abiotic values change
// must be able to change plant variables (ie max sunlight etc)
// BRUSH TOOL
// must save previous values
// must read in slider values and apply them to the maps
// ADD FILE READING! I HAVENT ADDED FILE READING FUNCTIONALITY TO THIS YET. WHEN YOU SELECT THE CHOOSE A FILE
    // OPTION ON MAINAPP IT USES ELLAS STARTUP WIZARD STUFF SO THE DISPLAY IS TOTALLY DIFFERENT
// hello




//*********** INSTANCE VARIABLES ****************\\
    private final Runnable onBack;
    private TestGrid testGrid;
    private SimulationResult currentResult;
    private boolean isTestGrid;
    
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
    
//*********** CONSTRUCTOR ****************\\
    public TestAndRunView(Runnable onBack, String mode, boolean isTestGrid, int sampleCount) {
        this.onBack = onBack;
        this.testGrid = new TestGrid(console::log, isTestGrid, sampleCount);
        this.isTestGrid = isTestGrid;
        
        setupUI(mode);
        console.hookSystemStreams();
    }
    

//*********** GENERAL METHODS ****************\\


    private void setupUI(String mode) {
        // Top toolbar
        setTop(buildToolbar(mode));
        
        // Center - split between tabs and console
        var logHeader = buildLogHeader();

        // this is the lhs thing with all of the windows, visual panes and terminal
        var logBox = new VBox(logHeader, console.getNode());
        
        var split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);
        split.getItems().addAll(tabs, logBox);
        split.setDividerPositions(0.65);
        
        // Main content - tabs on left, parameter panel on right
        HBox mainContent = new HBox(10);
    // buildParameterPanel is different between both
        if (isTestGrid) mainContent.getChildren().addAll(split, buildParameterPanelTest());
        else mainContent.getChildren().addAll(split, buildParameterPanelRun());
        HBox.setHgrow(split, Priority.ALWAYS);
        
        setCenter(mainContent);
        
        // Status bar at bottom
        setBottom(buildStatusBar());
        
        setPadding(new Insets(8));
    }
    
    private ToolBar buildToolbar(String mode) {
        Button backBtn = new Button("Back to Mode Selection");
        backBtn.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });
        
        Label title = new Label("Test Mode: " + mode);
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
    
    private HBox buildStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f0f0f0;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel.setFont(Font.font("Monospace", 11));
        statusBar.getChildren().add(statusLabel);
        
        return statusBar;
    }

    // Create visualization tabs at the top
    public void createTabs()
    {
        tabs.getTabs().clear();

        tabs.getTabs().add(makeTab("Forest + Elevation", new ScrollPane(new ForestOnTerrainView(currentResult.forest(), currentResult.elevationGrid(), currentResult.gridSpacing()))));

        tabs.getTabs().add(makeTab("Pink Noise", new ScrollPane(new PinkNoiseView(currentResult.samples(), currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()))));
        
        tabs.getTabs().add(makeTab("Forest", new ScrollPane(new ForestView(currentResult.forest(), currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()))));
            

    }

    // this updates the three tabs on the top and makes the grids
    private void updateVisualization() {
        tabs.getTabs().clear();
        
        createTabs();
    
        // bottom info not used for run
        if(isTestGrid) updateStatusDisplay();
        console.log("Plants: " + currentResult.forest().getAllPlants().size());
    }

    // This is just for the second screen with the options 
    public void initializeWithMode(String mode) {
        console.clear();
        console.log("Initializing Test Mode: " + mode);
        
        if (isTestGrid)
        {
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
        else
        {

            switch (mode){
                case "preset1":
                    executeSimulation(1, false);
                    break;
                case "preset2":
                    executeSimulation(2, false);
                    break;
                case "preset3":
                    executeSimulation(3, false);
                case "preset4":
                    executeSimulation(4, false);
                case "chooseFolder":
                    executeSimulation(5, false);
            }
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
                float[][] newTemp = gridEditors.get("Temperature").getValues();
                float[][] newAge = gridEditors.get("Age").getValues();
                float[][] newMoist = gridEditors.get("Moisture").getValues();
                float[][] newSun = gridEditors.get("Sunlight").getValues();
                float[][] newElev = gridEditors.get("Elevation").getValues();

                testGrid.setTemperatureGrid(newTemp);
                testGrid.setAgeGrid(newAge);
                testGrid.setMoistureGrid(newMoist);
                testGrid.setSunlightGrid(newSun);
                testGrid.setElevationGrid(newElev);

                currentResult = testGrid.runChange(regeneratePinkNoise.isSelected() 
                // ,regenerateSpecies.isSelected()
                );
            }
            else
            {
                currentResult = testGrid.run(choice);
            
            }

            // Update grid editors with current values
            if (isTestGrid) updateGridEditors();
            // else updateSliderEditors();
            
            createTabs();
            


            if (isTestGrid) updateStatusDisplay();
            console.log("✓ Test simulation complete. Plants placed: " + currentResult.forest().getAllPlants().size());

            int numSpecies = 0;
            for (SpeciesMap sm : currentResult.forest().getOverallSpeciesMap()) 
            {
                console.log(" - " + sm.getPlants().size() + "  " + sm.getSpecies().getName() );
                if (sm.getPlants().size() > 0) numSpecies++;
            }
            
            console.log("Number of species: " + numSpecies);
            updateVisualization();

        } catch (Exception ex) 
        {
            ex.printStackTrace();
            console.log("✗ Test simulation failed: " + ex.getMessage());
        }
    }

    private Tab makeTab(String name, Node content) {
        Tab t = new Tab(name, content);
        t.setClosable(false);
        return t;
    }



//*********** RUN VIEW METHODS ****************\\
    


    private VBox buildParameterPanelRun(){

        final Map<String, CheckBox> speciesCheck = new HashMap<>();
        parameterPanel.setPadding(new Insets(10));
        parameterPanel.setPrefWidth(500);
        parameterPanel.setStyle("-fx-font-family: Consolas, 'Courier New', monospace; -fx-font-size: 12px;");
        
        Label title = new Label("Parameter Controls");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(parameterPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(700);
        
        // Create species checkboxes
        speciesCheck.put("boxwood", new CheckBox("Boxwood"));
        speciesCheck.put("snowyMespilus", new CheckBox("Snowy Mespilus"));
        speciesCheck.put("mountainPine", new CheckBox("Mountain Pine"));
        speciesCheck.put("silverFir", new CheckBox("Silver Fir"));
        speciesCheck.put("silverBirch", new CheckBox("Silver Birch"));
        speciesCheck.put("sissileOak", new CheckBox("Sissile Oak"));
        speciesCheck.put("europeanBeech", new CheckBox("European Beech"));

        Label temp = new Label("Temperature");
        Slider tempSlider = new Slider(-10, 40, -10);
        tempSlider.setShowTickLabels(true);
        tempSlider.setShowTickMarks(true);
        tempSlider.setBlockIncrement(0.5);
        tempSlider.setPrefWidth(250);
        
        Label enivroAge = new Label("Environnment Age");
        Slider ageSlider = new Slider(1, 600, 1);
        ageSlider.setShowTickLabels(true);
        ageSlider.setShowTickMarks(true);
        ageSlider.setBlockIncrement(1);
        ageSlider.setPrefWidth(250);

        Label sun = new Label("Sun");
        Slider sunSlider = new Slider(3, 7, 3);
        sunSlider.setShowTickLabels(true);
        sunSlider.setShowTickMarks(true);
        sunSlider.setMajorTickUnit(0.25);
        sunSlider.setBlockIncrement(0.25);
        sunSlider.setSnapToTicks(true);
        sunSlider.setMinorTickCount(0);
        sunSlider.showTickLabelsProperty();
        sunSlider.setPrefWidth(250);

        GridPane gridPane = new GridPane();
        int col = 0, row = 0;
        
        for (CheckBox boxes : speciesCheck.values()) {
            boxes.setSelected(true);
            gridPane.add(boxes, col, row);
            col++;
            if (col > 1) { // wrap after 2 columns
                col = 0;
                row++;
            }
        }

        Label brushSize = new Label("Brush Size");
        Slider brushSizeSlider = new Slider(1, 5, 1);
        brushSizeSlider.setShowTickLabels(true);
        brushSizeSlider.setShowTickMarks(true);
        brushSizeSlider.setMajorTickUnit(1);
        brushSizeSlider.setBlockIncrement(1);
        brushSizeSlider.setPrefWidth(250);
        brushSizeSlider.setSnapToTicks(true);
        brushSizeSlider.setMinorTickCount(0);
        brushSizeSlider.showTickLabelsProperty();
        

        Button simulateBtn = new Button("Selected Species Only");
        simulateBtn.setOnAction(e -> removeSpecies(currentResult, speciesCheck));
        simulateBtn.setPrefWidth(250);

        VBox panelContent = new VBox(10);
        panelContent.getChildren().addAll
        (
            title,
            new Separator(),
            temp,
            tempSlider,
            new Separator(),
            enivroAge,
            ageSlider,
            new Separator(),
            sun,
            sunSlider,
            new Separator(),
            gridPane,
            new Separator(),
            brushSize,
            brushSizeSlider,
            new Separator(),
            simulateBtn);
        

        scrollPane.setContent(panelContent);
        
        VBox container = new VBox(scrollPane);
        return container;

    }

    private void removeSpecies(SimulationResult result, Map<String, CheckBox> speciesCheck){
        for (CheckBox boxes : speciesCheck.values()) {
            if(boxes.isSelected()!=true && result.forest().removedSpecies.containsKey(boxes.getText()) != true){
                result.forest().removeSpecies(boxes.getText());
            }else if(boxes.isSelected()){
                if(result.forest().removedSpecies.containsKey(boxes.getText())){
                    result.forest().addSpeciesMapByName(boxes.getText());
                }          
            }
        }
        createTabs();
    }
    


//*********** TEST VIEW METHODS****************\\



    private VBox buildParameterPanelTest() {
        parameterPanel.setPadding(new Insets(10));
        parameterPanel.setPrefWidth(500);
        parameterPanel.setStyle("-fx-border-color: #ca9292ff; -fx-border-width: 1;");
        
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
        
    // puts in the values for the right tab
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

}