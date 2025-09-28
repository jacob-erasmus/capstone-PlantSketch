
package plantsketch.ui;

import plantsketch.*;
import plantsketch.util.PerformanceTimer;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Interface extends BorderPane {
    

//*********** NOTES AND TO DO ****************\\

// must be able to change plant variables (ie max sunlight etc)
// must read in slider values and apply them to the maps
// edit species variables.
// i have added getters and setters so i simply need to just edit speciesList values in TestGrid (global species changes)
// make checking/ unchecking species automatic on button click


// species editing:
    // pull down menu for each species
    // be able to edit each value for the species
    // when done, it'll run
    // on button click (explain it is on button click because it is global)
    // brush is still automatic though and on the brush stroke

    // WHEN UNDO UPDATE SPECIES PARAMETERS TAB


//*********** INSTANCE VARIABLES ****************\\
    

    private ArrayList<SimulationResult> saveStatesArray = new ArrayList<>();
    private int saveState;
    private boolean isUndo;
    private int maxSaveStates = 30;

    private final Runnable onBack;
    private SimulationEngine simulationEngine;
    private SimulationResult currentResult;
    private boolean isTestGrid;
    private int sampleCount;
    private boolean brushRemovalMode = false;
    private boolean brushAgeMode = false;
    private ForestOnMapView forestElevationView;
    private ForestOnMapView forestTemperatureView;
    private ForestOnMapView forestSunlightView;
    private ForestOnMapView forestMoistureView;
    private ForestOnMapView forestAgeView;
    private ForestView forestView;
    
    Supplier<Set<String>> getSelectedSpecies;
    GridPane speciesPanelEditor;
    String[] speciesParameters = {"sunlightC", "sunlightR", "moistureC", "moistureR", "temperatureC", "temperatureR", "slopeC", "slopeR", "maxHeightOpen", "maxHeightClosed", "q", "lifeSpan"};
    TextField[] textFields;
    // UI Components
    private final TabPane tabs = new TabPane();
    private final ConsolePane console = new ConsolePane();
    private final Label statusLabel = new Label();
    private final CheckBox regeneratePinkNoise = new CheckBox("Re-generate pink noise?");
    // private final CheckBox regenerateSpecies = new CheckBox("Re-generate species placement?");
    
    // Grid value editors
    private final Map<String, GridEditor> gridEditors = new HashMap<>();
    private final Map<String, Slider> sliders = new HashMap<>();
    
//*********** CONSTRUCTOR ****************\\


    public Interface(Runnable onBack, String mode, boolean isTestGrid, int sampleCount) {
        this.onBack = onBack;
        this.sampleCount = sampleCount;
        this.simulationEngine = new SimulationEngine(console::log, isTestGrid, sampleCount);
        this.isTestGrid = isTestGrid;
        isUndo = false;
        saveState = 0;
        
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
        split.setDividerPositions(0.666);
        
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

        Button perfBtn = new Button("Performance Stats");
        perfBtn.setOnAction(e -> {
            PerformanceTimer.printStats();
        });

        Label title = new Label("Test Mode: " + mode);
        title.setFont(Font.font("System", FontWeight.BOLD, 16));

        return new ToolBar(backBtn, new Separator(), perfBtn, new Separator(), title);
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
        createForestOnMapTab(currentResult.terrain().getElevationGrid(), "Environment (Elevation)");
        createForestOnMapTab(currentResult.abiotics().getMoistureMap().getGrid(), "Moisture");
        createForestOnMapTab(currentResult.abiotics().getSunlightMap().getGrid(), "Sunlight");
        createForestOnMapTab(currentResult.abiotics().getTemperatureMap().getGrid(), "Temperature");
        createForestOnMapTab(simulationEngine.getAgeGrid(), "Age");
        createPinkNoiseTab();
        createForestTab();

    }
    

    // Create Forest + Elevation tab with zoom controls
    private void createForestOnMapTab(float[][] map, String mapType){
        VBox mapContainer = new VBox(5);
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

        //Create the view
        ScrollPane mapPane = null;
        switch (mapType) {
                case "Environment (Elevation)":
                    this.forestElevationView = new ForestOnMapView(currentResult.forest(), map, currentResult.gridSpacing());
                    mapPane = new ScrollPane(forestElevationView);
                    
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

                    mapContainer.getChildren().addAll(zoomControls, mapPane);
                    break;

                case "Moisture":
                    this.forestMoistureView = new ForestOnMapView(currentResult.forest(), map, currentResult.gridSpacing());
                    mapPane = new ScrollPane(forestMoistureView);

                    // Zoom track
                    final double[] zoomMLevel = {1.0};
                    zoomInBtn.setOnAction(e -> { 
                        //limit
                        if(forestMoistureView.getHeight() != 3000){
                            zoomMLevel[0] = zoomMLevel[0] * 1.2;
                            forestMoistureView.zoomIn();
                            zoomLabel.setText(String.format("%.0f%%", zoomMLevel[0] * 100));
                        }else{
                            zoomLabel.setText("Max Zoom");
                        }
                    });
                    zoomOutBtn.setOnAction(e -> {
                        if(forestMoistureView.getHeight() != 256){
                            zoomMLevel[0] = zoomMLevel[0] / 1.2;
                            forestMoistureView.zoomOut();
                            zoomLabel.setText(String.format("%.0f%%", zoomMLevel[0] * 100));
                        }else{
                            zoomLabel.setText("Min Zoom");
                        }
                    });
                    defaultBtn.setOnAction(e -> {
                        zoomMLevel[0] = 1.0;
                        forestMoistureView.resetToDefault();
                        zoomLabel.setText("100%");
                    });
                    
                    mapContainer.getChildren().addAll(zoomControls, mapPane);
                    break;
                case "Sunlight":
                    this.forestSunlightView = new ForestOnMapView(currentResult.forest(), map, currentResult.gridSpacing());
                    mapPane = new ScrollPane(forestSunlightView);

                    // Zoom track
                    final double[] zoomSLevel = {1.0};
                    zoomInBtn.setOnAction(e -> { 
                        //limit
                        if(forestSunlightView.getHeight() != 3000){
                            zoomSLevel[0] = zoomSLevel[0] * 1.2;
                            forestSunlightView.zoomIn();
                            zoomLabel.setText(String.format("%.0f%%", zoomSLevel[0] * 100));
                        }else{
                            zoomLabel.setText("Max Zoom");
                        }
                    });
                    zoomOutBtn.setOnAction(e -> {
                        if(forestSunlightView.getHeight() != 256){
                            zoomSLevel[0] = zoomSLevel[0] / 1.2;
                            forestSunlightView.zoomOut();
                            zoomLabel.setText(String.format("%.0f%%", zoomSLevel[0] * 100));
                        }else{
                            zoomLabel.setText("Min Zoom");
                        }
                    });
                    defaultBtn.setOnAction(e -> {
                        zoomSLevel[0] = 1.0;
                        forestSunlightView.resetToDefault();
                        zoomLabel.setText("100%");
                    });
                    
                    mapContainer.getChildren().addAll(zoomControls, mapPane);
                    break;
                case "Temperature":
                    this.forestTemperatureView = new ForestOnMapView(currentResult.forest(), map, currentResult.gridSpacing());
                    mapPane = new ScrollPane(forestTemperatureView);

                    // Zoom track
                    final double[] zoomTLevel = {1.0};
                    zoomInBtn.setOnAction(e -> { 
                        //limit
                        if(forestTemperatureView.getHeight() != 3000){
                            zoomTLevel[0] = zoomTLevel[0] * 1.2;
                            forestTemperatureView.zoomIn();
                            zoomLabel.setText(String.format("%.0f%%", zoomTLevel[0] * 100));
                        }else{
                            zoomLabel.setText("Max Zoom");
                        }
                    });
                    zoomOutBtn.setOnAction(e -> {
                        if(forestTemperatureView.getHeight() != 256){
                            zoomTLevel[0] = zoomTLevel[0] / 1.2;
                            forestTemperatureView.zoomOut();
                            zoomLabel.setText(String.format("%.0f%%", zoomTLevel[0] * 100));
                        }else{
                            zoomLabel.setText("Min Zoom");
                        }
                    });
                    defaultBtn.setOnAction(e -> {
                        zoomTLevel[0] = 1.0;
                        forestTemperatureView.resetToDefault();
                        zoomLabel.setText("100%");
                    });

                    mapContainer.getChildren().addAll(zoomControls, mapPane);
                    break;
                case "Age":
                    this.forestAgeView = new ForestOnMapView(currentResult.forest(), map, currentResult.gridSpacing());
                    mapPane = new ScrollPane(forestAgeView);

                    // Zoom track
                    final double[] zoomALevel = {1.0};
                    zoomInBtn.setOnAction(e -> {
                        //limit
                        if(forestAgeView.getHeight() != 3000){
                            zoomALevel[0] = zoomALevel[0] * 1.2;
                            forestAgeView.zoomIn();
                            zoomLabel.setText(String.format("%.0f%%", zoomALevel[0] * 100));
                        }else{
                            zoomLabel.setText("Max Zoom");
                        }
                    });
                    zoomOutBtn.setOnAction(e -> {
                        if(forestAgeView.getHeight() != 256){
                            zoomALevel[0] = zoomALevel[0] / 1.2;
                            forestAgeView.zoomOut();
                            zoomLabel.setText(String.format("%.0f%%", zoomALevel[0] * 100));
                        }else{
                            zoomLabel.setText("Min Zoom");
                        }
                    });
                    defaultBtn.setOnAction(e -> {
                        zoomALevel[0] = 1.0;
                        forestAgeView.resetToDefault();
                        zoomLabel.setText("100%");
                    });

                    mapContainer.getChildren().addAll(zoomControls, mapPane);
                    break;
            }
        mapPane.setFitToHeight(false);
        mapPane.setFitToWidth(false);
        mapPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        mapPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);     
        VBox.setVgrow(mapPane, Priority.ALWAYS);
        tabs.getTabs().add(makeTab(mapType, mapContainer));
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
        forestView = new ForestView(currentResult.forest(), currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing()); 
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
        // console.log("Initializing Test Mode: " + mode);
        
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
                    simulationEngine = new SimulationEngine(console::log, isTestGrid, sampleCount);
                    console.log("Custom folder mode - waiting for folder data...");
                    executeSimulation(5, false, null);
            }
        }
    }

    // here is execute simulation for the first time
    private void executeSimulation(int choice, boolean isResimulation, String fullPath) {
        PerformanceTimer.start("execute_simulation");
        tabs.getTabs().clear();

        try {
            if (!isUndo)
            {
                // making a new 'branch' as the user has made an edit. can no longer redo
                if(saveStatesArray.size() -1 > saveState)
                {
                    for (int i = saveState; i < saveStatesArray.size(); i++)
                    {
                        saveStatesArray.remove(i);
                    }
                }
                

                if(isResimulation)
                {
                    // index of which save state it is
                        saveState++;
                        console.log("Re-simulating with new parameters...");
                    if(isTestGrid)
                    {
                        boolean wasChange = readGridEditors();
                        // Update TestGrid with new values
                        if (regeneratePinkNoise.isSelected()) currentResult = simulationEngine.runChange(regeneratePinkNoise.isSelected(), false); // if need to regenerate pink noise
                        else if (wasChange) currentResult = simulationEngine.runChange(regeneratePinkNoise.isSelected(), false); // if there was actually a change to anything
                    }
                }
                else
                {
                    currentResult = simulationEngine.run(choice, fullPath);
                
                }
                // adds to the current save state
                saveStatesArray.add(currentResult);
                if (saveStatesArray.size() > maxSaveStates) // maximum 30 save states
                {
                    saveStatesArray.remove(1); // keeps the original forest but removes the first iteration on top of that
                    saveState--; // the index now caps out at 20

                }
            }

            
            // for undo/redo of main run
            if (isUndo && !isTestGrid && isResimulation){
                currentResult = simulationEngine.runChange(false, true);
            }

            // Update grid editors with current values
            if (isTestGrid) 
            {
                updateGridEditors();
                updateStatusDisplay();

            }
            // else updateSliderEditors();
            
            updateSpeciesPanelEditor();

            createTabs();
            
            // console.log("✓ Simulation complete. Plants placed: " + currentResult.forest().getAllPlants().size());

            int numSpecies = 0;
            for (SpeciesMap sm : currentResult.forest().getOverallSpeciesMap()) 
            {
                console.log(" - " + sm.getPlants().size() + "  " + sm.getSpecies().getName() );
                if (sm.getPlants().size() > 0) numSpecies++;
            }
            
            console.log("Number of species: " + numSpecies);
            isUndo = false;
            updateVisualization();

        } catch (Exception ex)
        {
            ex.printStackTrace();
            console.log("✗ Test simulation failed: " + ex.getMessage());
        } finally {
            PerformanceTimer.end("execute_simulation");
        }
    }

    private Tab makeTab(String name, Node content) {
        Tab t = new Tab(name, content);
        t.setClosable(false);
        return t;
    }

    private void undo()
    {
        if(saveStatesArray.size() > 1 && saveState > 0)
        {
            isUndo = true;

            // saveStatesArray.remove(saveState--); // removes last saveState then decrements by 1
            saveState--;
            System.out.println("Save State: " + saveState);
            currentResult = saveStatesArray.get(saveState);
            this.simulationEngine.loadSaveState(currentResult);
            executeSimulation(0, true, null); // choice doesnt matter because it
        }

    }

    /*
        so the way undo and redo are going to work is this:
            say you work for 6 save states
            then undo 2
            you can now redo those two undos and get back to 6 save states
            BUT
            as soon as you make a new change it will make a new 'branch' and you lose all the old save states that you undid and you cannot redo them

     */
    private void redo()
    {
        if(saveStatesArray.size() > 1 && saveState +1 < saveStatesArray.size())
        {
            isUndo = true;

            saveState++;
            System.out.println("Save State: " + saveState);
            currentResult = saveStatesArray.get(saveState);
            this.simulationEngine.loadSaveState(currentResult);
            executeSimulation(0, true, null); // choice doesnt matter because it
        }
    }

    // makes the species panel editors
    private GridPane createSpeciesPanelEditor(Species species)
    {

        speciesPanelEditor = new GridPane();
        speciesPanelEditor.setHgap(10);
        speciesPanelEditor.setVgap(8);

        float[] parameterValues = {species.getSunlightC(), species.getSunlightR(), species.getMoistureC(), species.getMoistureR(), 
            species.getTemperatureC(), species.getTemperatureR(), species.getSlopeC(), species.getSlopeR(), species.getMaxHeightOpen(),
            species.getMaxHeightClosed(), species.getQ(), species.getLifeSpan()};

        textFields = new TextField[speciesParameters.length];

        // how tf are we supposed to directly change viability? Maybe add that to the brush or something?

        int row = 1;
        for (int i = 0; i < speciesParameters.length; i++) {
            int index = i;  // final copy for lambda

            Label titleLabel = new Label(speciesParameters[i]);
            titleLabel.setPrefWidth(200);
            speciesPanelEditor.add(titleLabel, 0, index);

            TextField parameterValue = new TextField();
            parameterValue.setPromptText(String.valueOf(parameterValues[index]));
            parameterValue.setPrefWidth(200);

// POTENTIALLY NOT WORKING
            parameterValue.textProperty().addListener((obs, oldText, newText) -> {
                if (!newText.trim().isEmpty()) {
                    try {
                        float value = Float.parseFloat(newText.trim());

                        // use index instead of i
                        if (index == 0) species.setSunlightC(value);
                        else if (index == 1) species.setSunlightR(value);
                        else if (index == 2) species.setMoistureC(value);
                        else if (index == 3) species.setMoistureR(value);
                        else if (index == 4) species.setTemperatureC(value);
                        else if (index == 5) species.setTemperatureR(value);
                        else if (index == 6) species.setSlopeC(value);
                        else if (index == 7) species.setSlopeR(value);
                        else if (index == 8) species.setMaxHeightOpen(value);
                        else if (index == 9) species.setMaxHeightClosed(value);
                        else if (index == 10) species.setQ(value);
                        else if (index == 11) species.setLifeSpan(value);

                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid float: " + newText);
                    }
                }
            });

            speciesPanelEditor.add(parameterValue, 1, index);
            textFields[i] = parameterValue;
        }


        return speciesPanelEditor;

    }

// NOT WORKING
    public GridPane updateSpeciesPanelEditor()
    {
        for (int i = 0; i < simulationEngine.getSpeciesList().size(); i++)
        {
            float[] parameterValues = {simulationEngine.getSpeciesList().get(i).getSunlightC(), simulationEngine.getSpeciesList().get(i).getSunlightR(), simulationEngine.getSpeciesList().get(i).getMoistureC(), simulationEngine.getSpeciesList().get(i).getMoistureR(),
            simulationEngine.getSpeciesList().get(i).getTemperatureC(), simulationEngine.getSpeciesList().get(i).getTemperatureR(), simulationEngine.getSpeciesList().get(i).getSlopeC(), simulationEngine.getSpeciesList().get(i).getSlopeR(), simulationEngine.getSpeciesList().get(i).getMaxHeightOpen(),
            simulationEngine.getSpeciesList().get(i).getMaxHeightClosed(), simulationEngine.getSpeciesList().get(i).getQ(), simulationEngine.getSpeciesList().get(i).getLifeSpan()};

            for (int j = 0; j < speciesParameters.length; j++) {
            textFields[j].setPromptText(String.valueOf(parameterValues[j]));
        }
        }


        

        return null;
    }

    public Button updateSpeciesParametersButton()
    {
        Button updateSpeciesParametersButton = new Button ("Update species parameters");

        updateSpeciesParametersButton.setOnAction(e -> {
            
            for (Species species : simulationEngine.getSpeciesList())
            {
                createSpeciesPanelEditor(species);
            }
            
            executeSimulation(0, true, null);
        }
            );
            
        return updateSpeciesParametersButton;
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


        // putting it all together
        Accordion accordion = new Accordion();

        Accordion innerAccordion = new Accordion();

        accordion.getPanes().add(createAbioticsPanel());

        for (Species species : simulationEngine.getSpeciesList())
        {

            GridPane speciesParametersPanel = createSpeciesPanelEditor(species);

            TitledPane speciesHeader = new TitledPane(species.getName(), speciesParametersPanel);
            speciesHeader.setExpanded(false);

            innerAccordion.getPanes().add(speciesHeader);
        };

        TitledPane overallSpecies = new TitledPane("Species Editing", innerAccordion);
        overallSpecies.setExpanded(false);

        accordion.getPanes().addAll(createSpeciesPanel(), overallSpecies);  

        VBox container = new VBox(accordion,updateSpeciesParametersButton(), createStatesPanel());
        return container;

    }

    // undo and save stuff (states)
    private ScrollPane createStatesPanel()
    {

        HBox stateButtons = createStateButtons();

        // undo and save menu
        VBox stateContent = new VBox(10);
        stateContent.getChildren().addAll
        (
            stateButtons);

        ScrollPane statesPane = new ScrollPane();
        statesPane.setFitToWidth(true);
        statesPane.setFitToHeight(true);
        statesPane.setContent(stateContent);

        return statesPane;
    }

    private TitledPane createSpeciesPanel()
    {
        // species stuff:
        final Map<String, CheckBox> speciesCheck = new HashMap<>();

        CheckBox boxwood = new CheckBox("Boxwood");
        boxwood.setStyle("-fx-text-fill: red;");
        speciesCheck.put("Boxwood", boxwood);

        CheckBox snowyMespilus = new CheckBox("Snowy Mespilus");
        snowyMespilus.setStyle("-fx-text-fill: blue;");
        speciesCheck.put("Snowy Mespilus", snowyMespilus);

        CheckBox mountainPine = new CheckBox("Mountain Pine");
        mountainPine.setStyle("-fx-text-fill: green;");
        speciesCheck.put("Mountain Pine", mountainPine);

        CheckBox silverFir = new CheckBox("Silver Fir");
        silverFir.setStyle("-fx-text-fill: purple;");
        speciesCheck.put("Silver Fir", silverFir);

        CheckBox silverBirch = new CheckBox("Silver Birch");
        silverBirch.setStyle("-fx-text-fill: pink;");
        speciesCheck.put("Silver Birch", silverBirch);

        CheckBox sissileOak = new CheckBox("Sissile Oak");
        sissileOak.setStyle("-fx-text-fill: orange;");
        speciesCheck.put("Sissile Oak", sissileOak);

        CheckBox europeanBeech = new CheckBox("European Beech");
        europeanBeech.setStyle("-fx-text-fill: brown;");
        speciesCheck.put("European Beech", europeanBeech);

        Button simulateBtn = new Button("Selected Species Only");
        simulateBtn.setOnAction(e -> {
            saveState++;
            saveStatesArray.add(currentResult);
                if (saveStatesArray.size() > 20) // maximum 20 save states
                {
                    saveStatesArray.remove(1); // keeps the original forest but removes the first iteration on top of that
                    saveState--; // the index now caps out at 20

                }
            removeSpecies(currentResult, speciesCheck);});
        simulateBtn.setPrefWidth(250);

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
        // define the supplier that always reflects current checkbox states
        this.getSelectedSpecies = () -> speciesCheck.entrySet().stream()
            .filter(entry -> entry.getValue().isSelected())
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        // species window
        VBox speciesContent = new VBox(10);
        speciesContent.getChildren().addAll
        (
            gridPane,
            new Separator(),
            simulateBtn,
            new Separator());

        ScrollPane speciesPane = new ScrollPane();
        speciesPane.setFitToWidth(true);
        speciesPane.setFitToHeight(true);
        speciesPane.setContent(speciesContent);

        TitledPane speciesHeader = new TitledPane("Species Selecting", speciesPane);
        speciesHeader.setExpanded(false);

        return speciesHeader;
    }

    private TitledPane createAbioticsPanel()
    {
         // abiotic stuff:
        Label temp = new Label("Temperature: Set Amount to Add or Subtract (Min = "+ simulationEngine.getMinTemp() +"  ; Max = "+ simulationEngine.getMaxTemp() +")");
        Slider tempSlider = new Slider(-15, 15, 0);
        tempSlider.setShowTickLabels(true);
        tempSlider.setShowTickMarks(true);
        tempSlider.setBlockIncrement(0.5);
        tempSlider.setMajorTickUnit(0.5);
        tempSlider.setSnapToTicks(true);
        tempSlider.setPrefWidth(400);
        
        Label age = new Label("Age: Set Amount to Add or Subtract (Min = "+ simulationEngine.getMinAge() +"  ; Max = "+ simulationEngine.getMaxAge() +")");
        Slider ageSlider = new Slider(-650, 650, 0);
        ageSlider.setShowTickLabels(true);
        ageSlider.setShowTickMarks(true);
        ageSlider.setBlockIncrement(25);
        ageSlider.setMajorTickUnit(25);
        ageSlider.setSnapToTicks(true);
        ageSlider.setPrefWidth(250);

        Label sun = new Label("Sunlight: Set Amount to Add or Subtract (Min = "+ simulationEngine.getMinSun() +"  ; Max = "+ simulationEngine.getMaxSun() +")");
        Slider sunSlider = new Slider(-13, 13, 0);
        sunSlider.setShowTickLabels(true);
        sunSlider.setShowTickMarks(true);
        sunSlider.setBlockIncrement(1);
        sunSlider.setMajorTickUnit(0.5);
        sunSlider.setSnapToTicks(true);
        sunSlider.setPrefWidth(250);

        Label moist = new Label("Moisture: Set Amount to Add or Subtract (Min = "+ simulationEngine.getMinMoist() +"  ; Max = "+ simulationEngine.getMaxMoist() +")");
        Slider moistSlider = new Slider(-54, 54, 0);
        moistSlider.setShowTickLabels(true);
        moistSlider.setShowTickMarks(true);
        moistSlider.setBlockIncrement(1);
        moistSlider.setMajorTickUnit(1);
        moistSlider.setSnapToTicks(true);
        moistSlider.setPrefWidth(250);

        Label elevation = new Label("Elevation: Set Amount to Add or Subtract (Min = "+ simulationEngine.getMinElev() +"  ; Max = "+ simulationEngine.getMaxElev() +")");
        Slider elevationSlider = new Slider(-200, 200, 0);
        elevationSlider.setShowTickLabels(true);
        elevationSlider.setShowTickMarks(true);
        elevationSlider.setBlockIncrement(10);
        elevationSlider.setMajorTickUnit(10);
        elevationSlider.setSnapToTicks(true);
        elevationSlider.setPrefWidth(250);

        // brush stuff
        Label brushSize = new Label("Brush Size");
        Slider brushSizeSlider = new Slider(10, 90, 50);
        brushSizeSlider.setShowTickLabels(true);
        brushSizeSlider.setShowTickMarks(true);
        brushSizeSlider.setMajorTickUnit(20);
        brushSizeSlider.setBlockIncrement(20);
        brushSizeSlider.setPrefWidth(250);
        brushSizeSlider.setSnapToTicks(true);
        brushSizeSlider.setMinorTickCount(0);
        brushSizeSlider.showTickLabelsProperty();
        
        //listen for changes
        brushSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (brushRemovalMode){
                switch(tabs.getSelectionModel().getSelectedItem().getText()){
                    case("Environment (Elevation)"):
                        updateBrushCursor(forestElevationView, newVal.doubleValue());
                        break;
                    case("Temperature"):
                        updateBrushCursor(forestTemperatureView, newVal.doubleValue());
                        break;
                    case("Sunlight"):
                        updateBrushCursor(forestSunlightView, newVal.doubleValue());
                        break;
                    case("Moisture"):
                        updateBrushCursor(forestMoistureView, newVal.doubleValue());
                        break;
                    case("Age"):
                        updateBrushCursor(forestAgeView, newVal.doubleValue());
                        break;
                }
                
            }
        });

        //listen for tab change, bringover brush changes and set brush off.
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                brushRemovalMode = false;
                brushAgeMode = false;
                saveState++;
                saveStatesArray.add(currentResult);
                if (saveStatesArray.size() > 20) // maximum 20 save states
                {
                    saveStatesArray.remove(1); // keeps the original forest but removes the first iteration on top of that
                    saveState--; // the index now caps out at 20

                }
                console.log("\nNumber of Remaining Plants: " + currentResult.forest().getAllPlants().size());
                switch(newTab.getText()){
                    case("Environment (Elevation)"):
                            forestElevationView.disableBrushMode();
                            forestElevationView.setCursor(Cursor.DEFAULT);  
                            forestElevationView.draw();
                            break;
                        case("Temperature"):
                            forestTemperatureView.disableBrushMode();
                            forestTemperatureView.setCursor(Cursor.DEFAULT);  
                            forestTemperatureView.draw();
                            break;
                        case("Sunlight"):
                            forestSunlightView.disableBrushMode();
                            forestSunlightView.setCursor(Cursor.DEFAULT);  
                            forestSunlightView.draw();
                            break;
                        case("Moisture"):
                            forestMoistureView.disableBrushMode();
                            forestMoistureView.setCursor(Cursor.DEFAULT);
                            forestMoistureView.draw();
                            break;
                        case("Age"):
                            forestAgeView.disableBrushMode();
                            forestAgeView.setCursor(Cursor.DEFAULT);
                            forestAgeView.draw();
                            break;
                        case("Pink Noise"):
                            System.out.println("Brush functions aren't applicabe for Pink Noise tab <-> FOR VISUAL PURPOSES ONLY");
                            break;
                        case("Forest"):
                            forestView.draw();
                            System.out.println("Brush functions can't be applied directly on Forest tab, only carried over <-> FOR VISUAL PURPOSES ONLY");
                            break;
                }
            }
        });

        Button enableBrushRemovalBtn = new Button("Toggle Brush Removal Mode");
        enableBrushRemovalBtn.setOnAction(e -> 
            {
            if (!brushRemovalMode){
                saveState++;
                saveStatesArray.add(currentResult);
                if (saveStatesArray.size() > 20) // maximum 20 save states
                {
                    saveStatesArray.remove(1); // keeps the original forest but removes the first iteration on top of that
                    saveState--; // the index now caps out at 20
                }
            }
            switch(tabs.getSelectionModel().getSelectedItem().getText()){
                    case("Environment (Elevation)"):
                        forestElevationView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushRemoval(forestElevationView, brushSizeSlider);
                        break;
                    case("Temperature"):
                        forestTemperatureView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushRemoval(forestTemperatureView, brushSizeSlider);
                        break;
                    case("Sunlight"):
                        forestSunlightView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushRemoval(forestSunlightView, brushSizeSlider);
                        break;
                    case("Moisture"):
                        forestMoistureView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushRemoval(forestMoistureView, brushSizeSlider);
                        break;
                    case("Age"):
                        forestAgeView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushRemoval(forestAgeView, brushSizeSlider);
                        break;
                    case("Pink Noise"):
                        System.out.println("Brush functions are disabled for Pink Noise tab <-> FOR VISUAL PURPOSES ONLY");
                        break;
                    case("Forest"):
                        System.out.println("Brush functions are disabled for Forest tab <-> FOR VISUAL PURPOSES ONLY");
                        break;
            }    });
        enableBrushRemovalBtn.setPrefWidth(250);

        Button enableAgeChangeBtn = new Button("Toggle Brush Age Mode");
        enableAgeChangeBtn.setOnAction(e -> 
            {
            if (!brushAgeMode){
                saveState++;
                saveStatesArray.add(currentResult);
                if (saveStatesArray.size() > 20) // maximum 20 save states
                {
                    saveStatesArray.remove(1); // keeps the original forest but removes the first iteration on top of that
                    saveState--; // the index now caps out at 20
                }
            }
            switch(tabs.getSelectionModel().getSelectedItem().getText()){
                    case("Environment (Elevation)"):
                        forestElevationView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushAge(forestElevationView, brushSizeSlider, ageSlider);
                        break;
                    case("Temperature"):
                        forestTemperatureView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushAge(forestTemperatureView, brushSizeSlider, ageSlider);
                        break;
                    case("Sunlight"):
                        forestSunlightView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushAge(forestSunlightView, brushSizeSlider, ageSlider);
                        break;
                    case("Moisture"):
                        forestMoistureView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushAge(forestMoistureView, brushSizeSlider, ageSlider);
                        break;
                    case("Age"):
                        forestAgeView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushAge(forestAgeView, brushSizeSlider, ageSlider);
                        break;
                    case("Pink Noise"):
                        System.out.println("Brush functions are disabled for Pink Noise tab <-> FOR VISUAL PURPOSES ONLY");
                        break;
                    case("Forest"):
                        System.out.println("Brush functions are disabled for Forest tab <-> FOR VISUAL PURPOSES ONLY");
                        break;
            }    });
        enableAgeChangeBtn.setPrefWidth(250);
        // menu for abiotics
        VBox abioticContent = new VBox(10);
        abioticContent.getChildren().addAll
        (
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
            brushSize,
            brushSizeSlider,
            enableBrushRemovalBtn,
            enableAgeChangeBtn,
            new Separator());

        ScrollPane slidersPane = new ScrollPane();
        slidersPane.setFitToWidth(true);
        slidersPane.setFitToHeight(true);
        slidersPane.setContent(abioticContent);

        TitledPane slidersHeader = new TitledPane("Abiotics Editing & Brush", slidersPane);
        slidersHeader.setExpanded(false);

        return slidersHeader;
    }

    private void brushRemoval(ForestOnMapView mapView, Slider brushSizeSlider){
        brushRemovalMode = !brushRemovalMode; // toggle on/off
        if (brushRemovalMode){
            mapView.enableBrushRemovalMode(() -> brushSizeSlider.getValue());
            updateBrushCursor(mapView, brushSizeSlider.getValue());
        }else{
            mapView.disableBrushMode();
            mapView.setCursor(Cursor.DEFAULT);  
            console.log("\nNumber of Remaining Plants: " + currentResult.forest().getAllPlants().size());
        }
        
    }

    private void brushAge(ForestOnMapView mapView, Slider brushSlider, Slider ageSlider){
        brushAgeMode = !brushAgeMode;
        if(brushAgeMode){
            mapView.enableBrushAgeMode((() -> brushSlider.getValue()), (() -> ageSlider.getValue()), simulationEngine);
            updateBrushCursor(mapView, brushSlider.getValue());
        }else{
            mapView.disableBrushMode();
            mapView.setCursor(Cursor.DEFAULT);  
            console.log("\nNumber of Remaining Plants: " + currentResult.forest().getAllPlants().size());
        }
    }
    private void updateBrushCursor(ForestOnMapView mapView, double brushSize){
//image as cursor - some glitches so commented out for now.
//Will fix/improve.
        //Image brushImage = new Image(getClass().getResource("/101064.png").toExternalForm());
        //double scale = brushSize;
        //Image scaledImg = new Image(brushImage.getUrl(), brushImage.getWidth() * scale, brushImage.getHeight() * scale, true, true);
        //forestElevationView.setCursor(new ImageCursor(scaledImg, scaledImg.getWidth()/2, scaledImg.getHeight()/2));
//basic crosshair cursor but no visual resize shown but functionally yes. 
        mapView.setCursor(Cursor.CROSSHAIR);

        
    }
    
    private void removeSpecies(SimulationResult result, Map<String, CheckBox> speciesCheck){
        long startTime = System.nanoTime();
        for (CheckBox boxes : speciesCheck.values()) {
            if(boxes.isSelected()!=true && result.forest().removedSpecies.containsKey(boxes.getText()) != true){
                result.forest().removeSpecies(boxes.getText());
            }else if(boxes.isSelected()){
                if(result.forest().removedSpecies.containsKey(boxes.getText())){
                    result.forest().addSpeciesMapByName(boxes.getText());

                    //moved saved state to the button call
                }          
            }
        }
        refreshForestViews();
        console.log("Species Filter Remove and Visualise Elapsed Time: " + (System.nanoTime() - startTime) + " nanoseconds.");
    }

    private void refreshForestViews() {
        PerformanceTimer.start("refresh_forest_views");

        // Only redraw forest-related views, not environment maps
        if (forestElevationView != null) forestElevationView.draw();
        if (forestTemperatureView != null) forestTemperatureView.draw();
        if (forestSunlightView != null) forestSunlightView.draw();
        if (forestMoistureView != null) forestMoistureView.draw();
        if (forestAgeView != null) forestAgeView.draw();
        if (forestView != null) forestView.draw();

        PerformanceTimer.end("refresh_forest_views");
    }

    public float readSlider(Slider slider)
    {
        return (float) slider.getValue();
    }
    
    private HBox createStateButtons()
    {
        Button undoButton = new Button("Undo");
        undoButton.setOnAction(e -> undo());
        undoButton.setPrefWidth(60);

        Button redoButton = new Button("Redo");
        redoButton.setOnAction(e -> redo());
        redoButton.setPrefWidth(60);

        TextField fileNameField = new TextField();
        fileNameField.setPromptText("Enter file name:");
        fileNameField.setPrefWidth(120);

        Button saveButton = new Button("Create .pdb file");
        saveButton.setOnAction(e -> {
            String name = fileNameField.getText().trim();
            if (!name.isEmpty()) 
            {
                new EcoVizOutput(currentResult).createFile( "src/outputPdbSaves/"+ name+".pdb");
            }});

        saveButton.setPrefWidth(100);

        Button[] stateButtons = {undoButton, redoButton, saveButton};

        HBox stateButtonRow = new HBox(5); // spacing of 5px between buttons
        stateButtonRow.getChildren().addAll(stateButtons);
        stateButtonRow.getChildren().add(fileNameField);

        return stateButtonRow;
    }


//*********** TEST VIEW METHODS****************\\
    

    // create test view abitic grids
    private ScrollPane createTestAbioticGrids()
    {
        VBox parameterPanel = new VBox(10);
        parameterPanel.setPadding(new Insets(10));
        parameterPanel.setPrefWidth(500);
        parameterPanel.setStyle("-fx-border-color: #ca9292ff; -fx-border-width: 1;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(parameterPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(700);
        
        // This creates the empty grids without the values inside
        gridEditors.put("Temperature", new GridEditor("Temperature (°C)", 
            simulationEngine.getMinTemp(), simulationEngine.getMaxTemp()));
        gridEditors.put("Age", new GridEditor("Age (years)", 
            simulationEngine.getMinAge(), simulationEngine.getMaxAge()));
        gridEditors.put("Moisture", new GridEditor("Moisture (%)", 
            simulationEngine.getMinMoist(), simulationEngine.getMaxMoist()));
        gridEditors.put("Sunlight", new GridEditor("Sunlight (hours)", 
            simulationEngine.getMinSun(), simulationEngine.getMaxSun()));
        gridEditors.put("Elevation", new GridEditor("Elevation (m)", 
            simulationEngine.getMinElev(), simulationEngine.getMaxElev()));
        gridEditors.put("Slope", new GridEditor("Slope (degrees)", 
            simulationEngine.getMinSlope(), simulationEngine.getMaxSlope()));
        
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
            gridPane,
            new Separator(),
            regeneratePinkNoise,
            // regenerateSpecies,
            simulateBtn);
        

        scrollPane.setContent(panelContent);
        return scrollPane;
    }




    // puts together the parameter panel for the testing grids
    private VBox buildParameterPanelTest() {

        ScrollPane scrollPane = createTestAbioticGrids();

        Accordion accordion = new Accordion();

        TitledPane abioticsHeader = new TitledPane("Abiotics Editing", scrollPane);
        abioticsHeader.setExpanded(true);

        accordion.getPanes().add(abioticsHeader);


        SpeciesDictionary dict = new SpeciesDictionary();
        List<Species> speciesList = List.of(
                dict.loadBoxwood(),
                dict.loadSnowyMespilus(),
                dict.loadMountainPine(),
                dict.loadSilverFir(),
                dict.loadSilverBirch(),
                dict.loadSissileOak(),
                dict.loadEuropeanBeech());

        for (Species species : speciesList)
        {

            GridPane speciesParametersPanel = createSpeciesPanelEditor(species);

            TitledPane speciesHeader = new TitledPane(species.getName(), speciesParametersPanel);
            speciesHeader.setExpanded(false);

            accordion.getPanes().add(speciesHeader);
        };

        VBox container = new VBox(accordion);

        container.getChildren().addAll(updateSpeciesParametersButton(), createStateButtons());

        return container;
    }
        
    // puts in the values for the right tab
    private void updateGridEditors() 
    {
        gridEditors.get("Temperature").setValues(simulationEngine.getTemperatureGrid());
        gridEditors.get("Age").setValues(simulationEngine.getAgeGrid());
        gridEditors.get("Moisture").setValues(simulationEngine.getMoistureGrid());
        gridEditors.get("Sunlight").setValues(simulationEngine.getSunlightGrid());
        gridEditors.get("Elevation").setValues(simulationEngine.getElevationGrid());
        gridEditors.get("Slope").setValues(simulationEngine.getSlopeGrid());
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
            simulationEngine.setTemperatureGrid(newTemp);
            wasChange = true;
        }
        if(gridEditors.get("Age").isEdited()) 
        {
            simulationEngine.setAgeGrid(newAge);
            wasChange = true;
        }        
        if(gridEditors.get("Moisture").isEdited()) 
        {
            simulationEngine.setMoistureGrid(newMoist);
            wasChange = true;
        }        
        if(gridEditors.get("Sunlight").isEdited()) 
        {
            simulationEngine.setSunlightGrid(newSun);
            wasChange = true;
        }        
        // Elevation editing disabled for performance - slope recalculation is expensive
        if(gridEditors.get("Elevation").isEdited())
        {
            console.log("Warning: Elevation editing disabled for performance reasons");
            // Reset elevation values to original state
            gridEditors.get("Elevation").setValues(simulationEngine.getElevationGrid());
        }

        return wasChange;
    }

    // This is the bottom status bar
    private void updateStatusDisplay() 
    {
        StringBuilder status = new StringBuilder();
        status.append("Grid Status | ");
        status.append("Samples: ").append(simulationEngine.getSampleCount()).append(" | ");
        status.append("Pink Noise: ").append(simulationEngine.getNumPinkNoise()).append(" | ");
        status.append("Plants: ").append(simulationEngine.getNumPlants()).append("\n");
        
        status.append("Temp: ").append(formatGrid(simulationEngine.getTemperatureGrid())).append(" | ");
        status.append("Age: ").append(formatGrid(simulationEngine.getAgeGrid())).append(" | ");
        status.append("Moist: ").append(formatGrid(simulationEngine.getMoistureGrid())).append("\n");
        status.append("Sun: ").append(formatGrid(simulationEngine.getSunlightGrid())).append(" | ");
        status.append("Elev: ").append(formatGrid(simulationEngine.getElevationGrid())).append(" | ");
        status.append("Slope: ").append(formatGrid(simulationEngine.getSlopeGrid()));
        
        statusLabel.setText(status.toString());
    }
    
    // formats the grid values for display in the bottom status bar
    private String formatGrid(float[][] grid) {
        if (grid == null || grid.length < 2) return "N/A";
        return String.format("[%.1f,%.1f;%.1f,%.1f]", grid[0][0], grid[0][1], grid[1][0], grid[1][1]);
    }

}