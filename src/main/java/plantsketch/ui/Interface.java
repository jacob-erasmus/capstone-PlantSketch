
// this class is the gui for the testing grid (2x2)

package plantsketch.ui;

import plantsketch.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Interface extends BorderPane {
    

//*********** NOTES AND TO DO ****************\\

// must be able to change plant variables (ie max sunlight etc)
// BRUSH TOOL
// must save previous values
// must read in slider values and apply them to the maps
// ADD FILE READING! I HAVENT ADDED FILE READING FUNCTIONALITY TO THIS YET. WHEN YOU SELECT THE CHOOSE A FILE
    // OPTION ON MAINAPP IT USES ELLAS STARTUP WIZARD STUFF SO THE DISPLAY IS TOTALLY DIFFERENT
// hello
// its me and i really really want to merge please




//*********** INSTANCE VARIABLES ****************\\
    private final Runnable onBack;
    private TestGrid testGrid;
    private SimulationResult currentResult;
    private boolean isTestGrid;
    private int sampleCount;
    private boolean brushRemovalMode = false;
    private ForestOnTerrainView forestElevationView;
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
    private final Map<String, Slider> sliders = new HashMap<>();
    
//*********** CONSTRUCTOR ****************\
    public Interface(Runnable onBack, String mode, boolean isTestGrid, int sampleCount) {
        this.onBack = onBack;
        this.sampleCount = sampleCount;
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
        // the forest and stuff
        split.getItems().addAll(tabs, logBox);
        split.setDividerPositions(0.80);
        
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
        createForestElevationTab();
        createPinkNoiseTab();
        createForestTab();

    }
    
    // Create Forest + Elevation tab with zoom controls
    private void createForestElevationTab(){
        VBox forestElevationContainer = new VBox(5);
        HBox zoomControls = new HBox(10);
        zoomControls.setAlignment(Pos.CENTER_LEFT);
        zoomControls.setPadding(new Insets(5));
        
        Button zoomInBtn = new Button("+");
        Button zoomOutBtn = new Button("-");
        Button defaultBtn = new Button("Default");
        Label zoomLabel = new Label("100%");
        
        zoomInBtn.setPrefSize(30, 30);
        zoomOutBtn.setPrefSize(30, 30);
        defaultBtn.setPrefSize(100, 30);
        
        zoomControls.getChildren().addAll(zoomOutBtn, zoomInBtn, defaultBtn, zoomLabel);
        
        // Create the view
        forestElevationView= new ForestOnTerrainView(currentResult.forest(), currentResult.elevationGrid(), currentResult.gridSpacing()); 
        ScrollPane forestElevationPane = new ScrollPane(forestElevationView);
        forestElevationPane.setFitToHeight(false);
        forestElevationPane.setFitToWidth(false);
        forestElevationPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        forestElevationPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        // Zoom track
        final double[] zoomLevel = {1.0};
        
        zoomInBtn.setOnAction(e -> { 
            //limit
            if(forestElevationView.getHeight() != 3000){
                zoomLevel[0] = zoomLevel[0] * 1.2;
                forestElevationView.zoomIn();
                zoomLabel.setText(String.format("%.0f%%", zoomLevel[0] * 100));
            }else{
                zoomLabel.setText("Max Zoom");
            }
        });

        zoomOutBtn.setOnAction(e -> {
            if(forestElevationView.getHeight() != 256){
                zoomLevel[0] = zoomLevel[0] / 1.2;
                forestElevationView.zoomOut();
                zoomLabel.setText(String.format("%.0f%%", zoomLevel[0] * 100));
            }else{
                zoomLabel.setText("Min Zoom");
            }
        });

        defaultBtn.setOnAction(e -> {
            zoomLevel[0] = 1.0;
            forestElevationView.resetToDefault();
            zoomLabel.setText("100%");
        });
        forestElevationContainer.getChildren().addAll(zoomControls, forestElevationPane);
        VBox.setVgrow(forestElevationPane, Priority.ALWAYS);
        tabs.getTabs().add(makeTab("Forest + Elevation", forestElevationContainer));
    }

    // Create Pink Noise tab with zoom controls
    private void createPinkNoiseTab(){
        VBox pinkNoiseContainer = new VBox(5);
        HBox zoomControls = new HBox(10);
        zoomControls.setAlignment(Pos.CENTER_LEFT);
        zoomControls.setPadding(new Insets(5));
        
        Button zoomInBtn = new Button("+");
        Button zoomOutBtn = new Button("-");
        Button defaultBtn = new Button("Default");
        Label zoomLabel = new Label("100%");
        
        zoomInBtn.setPrefSize(30, 30);
        zoomOutBtn.setPrefSize(30, 30);
        defaultBtn.setPrefSize(100, 30);
        
        zoomControls.getChildren().addAll(zoomOutBtn, zoomInBtn, defaultBtn, zoomLabel);
        
        // Create the view
        PinkNoiseView pinkNoiseView = new PinkNoiseView(currentResult.samples(), currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()); 
        ScrollPane pinkNoisePane = new ScrollPane(pinkNoiseView);
        pinkNoisePane.setFitToHeight(false);
        pinkNoisePane.setFitToWidth(false);
        pinkNoisePane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        pinkNoisePane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        // Zoom track
        final double[] zoomLevel = {1.0};
        
        zoomInBtn.setOnAction(e -> { 
            //limit
            if(pinkNoiseView.getHeight() != 3000){
                zoomLevel[0] = zoomLevel[0] * 1.2;
                pinkNoiseView.zoomIn();
                zoomLabel.setText(String.format("%.0f%%", zoomLevel[0] * 100));
            }else{
                zoomLabel.setText("Max Zoom");
            }
        });

        zoomOutBtn.setOnAction(e -> {
            if(pinkNoiseView.getHeight() != 256){
                zoomLevel[0] = zoomLevel[0] / 1.2;
                pinkNoiseView.zoomOut();
                zoomLabel.setText(String.format("%.0f%%", zoomLevel[0] * 100));
            }else{
                zoomLabel.setText("Min Zoom");
            }
        });

        defaultBtn.setOnAction(e -> {
            zoomLevel[0] = 1.0;
            pinkNoiseView.resetToDefault();
            zoomLabel.setText("100%");
        });
        pinkNoiseContainer.getChildren().addAll(zoomControls, pinkNoisePane);
        VBox.setVgrow(pinkNoisePane, Priority.ALWAYS);
        tabs.getTabs().add(makeTab("Pink Noise", pinkNoiseContainer));
    }
    
    // Create Forest tab with zoom controls
    private void createForestTab(){
        VBox forestContainer = new VBox(5);
        HBox zoomControls = new HBox(10);
        zoomControls.setAlignment(Pos.CENTER_LEFT);
        zoomControls.setPadding(new Insets(5));
        
        Button zoomInBtn = new Button("+");
        Button zoomOutBtn = new Button("-");
        Button defaultBtn = new Button("Default");
        Label zoomLabel = new Label("100%");
        
        zoomInBtn.setPrefSize(30, 30);
        zoomOutBtn.setPrefSize(30, 30);
        defaultBtn.setPrefSize(100, 30);
        
        zoomControls.getChildren().addAll(zoomOutBtn, zoomInBtn, defaultBtn, zoomLabel);
        
        // Create the view
        ForestView forestView = new ForestView(currentResult.forest(), currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()); 
        ScrollPane forestViewPane = new ScrollPane(forestView);
        forestViewPane.setFitToHeight(false);
        forestViewPane.setFitToWidth(false);
        forestViewPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        forestViewPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        // Zoom track
        final double[] zoomLevel = {1.0};
        
        zoomInBtn.setOnAction(e -> { 
            //limit
            if(forestView.getHeight() != 3000){
                zoomLevel[0] = zoomLevel[0] * 1.2;
                forestView.zoomIn();
                zoomLabel.setText(String.format("%.0f%%", zoomLevel[0] * 100));
            }else{
                zoomLabel.setText("Max Zoom");
            }
        });

        zoomOutBtn.setOnAction(e -> {
            if(forestView.getHeight() != 256){
                zoomLevel[0] = zoomLevel[0] / 1.2;
                forestView.zoomOut();
                zoomLabel.setText(String.format("%.0f%%", zoomLevel[0] * 100));
            }else{
                zoomLabel.setText("Min Zoom");
            }
        });

        defaultBtn.setOnAction(e -> {
            zoomLevel[0] = 1.0;
            forestView.resetToDefault();
            zoomLabel.setText("100%");
        });
        forestContainer.getChildren().addAll(zoomControls, forestViewPane);
        VBox.setVgrow(forestViewPane, Priority.ALWAYS);
        tabs.getTabs().add(makeTab("Forest", forestContainer));
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
                    executeSimulation(0, false, null);
                    break;
                case "preset1":
                    executeSimulation(1, false, null);
                    break;
                case "preset2":
                    executeSimulation(2, false, null);
                    break;
            }
        }
        else
        {

            switch (mode){
                case "preset1":
                    executeSimulation(1, false, null);
                    break;
                case "preset2":
                    executeSimulation(2, false, null);
                    break;
                case "preset3":
                    executeSimulation(3, false, null);
                case "preset4":
                    executeSimulation(4, false, null);
                case "chooseFolder":
                    testGrid = new TestGrid(console::log, isTestGrid, sampleCount);
                    console.log("Custom folder mode - waiting for folder data...");
                    executeSimulation(5, false, null);
            }
        }
    }

    // here is execute simulation for the first time
    private void executeSimulation(int choice, boolean isResimulation, String fullPath) {
        tabs.getTabs().clear();
        

        try {
            if(isResimulation)
            {
                console.log("Re-simulating with new parameters...");
            
                boolean wasChange = readGridEditors();
                // Update TestGrid with new values
                if (regeneratePinkNoise.isSelected()) currentResult = testGrid.runChange(regeneratePinkNoise.isSelected()); // if need to regenerate pink noise
                else if (wasChange) currentResult = testGrid.runChange(regeneratePinkNoise.isSelected()); // if there was actually a change to anything
            }
            else
            {
                currentResult = testGrid.run(choice, fullPath);
            
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
    
    public void initializeWithCustomFolder(Path dataRoot, String envFolder) {
        try {
            String fullPath = dataRoot.resolve(envFolder).toString();
            console.log("Initializing with custom folder: " + fullPath);

            executeSimulation(5, false, fullPath);
            
        } catch (Exception e) {
            console.log("Failed to load custom folder: " + e.getMessage());
            e.printStackTrace();
        }
}

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
        scrollPane.setPrefHeight(800);
        
        // Create species checkboxes
        speciesCheck.put("boxwood", new CheckBox("Boxwood"));
        speciesCheck.put("snowyMespilus", new CheckBox("Snowy Mespilus"));
        speciesCheck.put("mountainPine", new CheckBox("Mountain Pine"));
        speciesCheck.put("silverFir", new CheckBox("Silver Fir"));
        speciesCheck.put("silverBirch", new CheckBox("Silver Birch"));
        speciesCheck.put("sissileOak", new CheckBox("Sissile Oak"));
        speciesCheck.put("europeanBeech", new CheckBox("European Beech"));

        Label temp = new Label("Temperature Slider: Min = "+ testGrid.getMinTemp() +"  ; Max = "+ testGrid.getMaxTemp());
        Slider tempSlider = new Slider(-15, 15, 0);
        tempSlider.setShowTickLabels(true);
        tempSlider.setShowTickMarks(true);
        tempSlider.setBlockIncrement(0.5);
        tempSlider.setMajorTickUnit(0.5);
        tempSlider.setSnapToTicks(true);
        tempSlider.setPrefWidth(400);
        
        Label age = new Label("Age Slider: Min = "+ testGrid.getMinAge() +"  ; Max = "+ testGrid.getMaxAge());
        Slider ageSlider = new Slider(-650, 650, 0);
        ageSlider.setShowTickLabels(true);
        ageSlider.setShowTickMarks(true);
        ageSlider.setBlockIncrement(25);
        ageSlider.setMajorTickUnit(25);
        ageSlider.setSnapToTicks(true);
        ageSlider.setPrefWidth(250);

        Label sun = new Label("Sunlight Slider: Min = "+ testGrid.getMinSun() +"  ; Max = "+ testGrid.getMaxSun());
        Slider sunSlider = new Slider(-13, 13, 0);
        sunSlider.setShowTickLabels(true);
        sunSlider.setShowTickMarks(true);
        sunSlider.setBlockIncrement(1);
        sunSlider.setMajorTickUnit(0.5);
        sunSlider.setSnapToTicks(true);
        sunSlider.setPrefWidth(250);

        Label moist = new Label("Moisture Slider: Min = "+ testGrid.getMinMoist() +"  ; Max = "+ testGrid.getMaxMoist());
        Slider moistSlider = new Slider(-54, 54, 0);
        moistSlider.setShowTickLabels(true);
        moistSlider.setShowTickMarks(true);
        moistSlider.setBlockIncrement(1);
        moistSlider.setMajorTickUnit(1);
        moistSlider.setSnapToTicks(true);
        moistSlider.setPrefWidth(250);

        Label elevation = new Label("Elevation Slider: Min = "+ testGrid.getMinElev() +"  ; Max = "+ testGrid.getMaxElev());
        Slider elevationSlider = new Slider(-200, 200, 0);
        elevationSlider.setShowTickLabels(true);
        elevationSlider.setShowTickMarks(true);
        elevationSlider.setBlockIncrement(10);
        elevationSlider.setMajorTickUnit(10);
        elevationSlider.setSnapToTicks(true);
        elevationSlider.setPrefWidth(250);

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
        
        //listen for changes
        brushSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (brushRemovalMode){
                updateBrushCursor(forestElevationView, newVal.doubleValue());
            }
        });
        Button enableBrushRemovalBtn = new Button("Toggle Brush Removal Mode");
        enableBrushRemovalBtn.setOnAction(e -> brushRemoval(forestElevationView, brushSizeSlider));
        enableBrushRemovalBtn.setPrefWidth(250);

        Button simulateBtn = new Button("Selected Species Only");
        simulateBtn.setOnAction(e -> removeSpecies(currentResult, speciesCheck));
        simulateBtn.setPrefWidth(250);

        VBox panelContent = new VBox(10);
        panelContent.getChildren().addAll
        (
            title,
            new Separator(),
            elevation,
            elevationSlider,
            new Separator(),
            temp,
            tempSlider,
            new Separator(),
            age,
            ageSlider,
            new Separator(),
            sun,
            sunSlider,
            new Separator(),
            moist,
            moistSlider,
            new Separator(),
            gridPane,
            new Separator(),
            brushSize,
            brushSizeSlider,
            enableBrushRemovalBtn,
            new Separator(),
            simulateBtn);
        

        scrollPane.setContent(panelContent);
        
        VBox container = new VBox(scrollPane);
        return container;

    }

    private void brushRemoval(ForestOnTerrainView forestElevationView, Slider brushSizeSlider){
        brushRemovalMode = !brushRemovalMode; // toggle on/off
        if (brushRemovalMode){
            forestElevationView.enableBrushRemovalMode(() -> brushSizeSlider.getValue());
            updateBrushCursor(forestElevationView, brushSizeSlider.getValue());
        }else{
            forestElevationView.disableBrushRemovalMode();
            forestElevationView.setCursor(Cursor.DEFAULT);  
            console.log("\nNumber of Remaining Plants: " + currentResult.forest().getAllPlants().size());
        }
        
    }

    private void updateBrushCursor(ForestOnTerrainView forestElevationView, double brushSize){
//image as cursor - some glitches so commented out for now.
//Will fix/improve.
        //Image brushImage = new Image(getClass().getResource("/101064.png").toExternalForm());
        //double scale = brushSize;
        //Image scaledImg = new Image(brushImage.getUrl(), brushImage.getWidth() * scale, brushImage.getHeight() * scale, true, true);
        //forestElevationView.setCursor(new ImageCursor(scaledImg, scaledImg.getWidth()/2, scaledImg.getHeight()/2));
//basic crosshair cursor but no visual resize shown but functionally yes. 
        forestElevationView.setCursor(Cursor.CROSSHAIR);

        
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

    public float readSlider(Slider slider)
    {
        return (float) slider.getValue();
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
        simulateBtn.setOnAction(e -> executeSimulation(0, true, null));

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
    private void updateGridEditors() 
    {
        gridEditors.get("Temperature").setValues(testGrid.getTemperatureGrid());
        gridEditors.get("Age").setValues(testGrid.getAgeGrid());
        gridEditors.get("Moisture").setValues(testGrid.getMoistureGrid());
        gridEditors.get("Sunlight").setValues(testGrid.getSunlightGrid());
        gridEditors.get("Elevation").setValues(testGrid.getElevationGrid());
        gridEditors.get("Slope").setValues(testGrid.getSlopeGrid());
    }

    // reads in values from the screen grids
    private boolean readGridEditors()
    {
        boolean wasChange = false;

        float[][] newTemp = gridEditors.get("Temperature").getValues();
        float[][] newAge = gridEditors.get("Age").getValues();
        float[][] newMoist = gridEditors.get("Moisture").getValues();
        float[][] newSun = gridEditors.get("Sunlight").getValues();
        float[][] newElev = gridEditors.get("Elevation").getValues();

        if(gridEditors.get("Temperature").isEdited()) 
        {
            testGrid.setTemperatureGrid(newTemp);
            wasChange = true;
        }
        if(gridEditors.get("Age").isEdited()) 
        {
            testGrid.setAgeGrid(newAge);
            wasChange = true;
        }        
        if(gridEditors.get("Moisture").isEdited()) 
        {
            testGrid.setMoistureGrid(newMoist);
            wasChange = true;
        }        
        if(gridEditors.get("Sunlight").isEdited()) 
        {
            testGrid.setSunlightGrid(newSun);
            wasChange = true;
        }        
        if(gridEditors.get("Elevation").isEdited()) 
        {
            testGrid.setElevationGrid(newElev);
            wasChange = true;
        }

        return wasChange;
    }

    // This is the bottom status bar
    private void updateStatusDisplay() 
    {
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